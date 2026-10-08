import http from 'node:http';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { randomUUID } from 'node:crypto';
import {MINT,TOKEN,PROGRAM,CONFIG,decode,encode,wallet,units,stakeUnits,validatePayment,premiumStatus,Ledger} from './commerce.mjs';
const base=fileURLToPath(new URL('.',import.meta.url));
const ledger=new Ledger(process.env.DATA_DIR??resolve(base,'data'));
const now=()=>Math.floor(Date.now()/1000);
function config(){
 const c=JSON.parse(readFileSync(resolve(base,'config.json'),'utf8').replace(/^\uFEFF/,''));
 wallet(c.recipient);units(c.premiumPriceSkr);units(String(c.minimumStakeSkr));
 if(c.premiumDays!==30)throw Error('Only reviewed 30-day plan enabled');
 return c;
}
async function rpc(method,params){
 const url=process.env.SOLANA_RPC??'https://api.mainnet-beta.solana.com';
 if(!url.startsWith('https://'))throw Error('HTTPS RPC required');
 const r=await fetch(url,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({jsonrpc:'2.0',id:1,method,params}),signal:AbortSignal.timeout(18000)});
 if(!r.ok)throw Error('RPC temporarily unavailable');
 const j=await r.json();if(j.error)throw Error('RPC temporarily unavailable');return j.result;
}
async function mainnet(){if(await rpc('getGenesisHash',[])!=='5eykt4UsFv8P8NJdTREpY1vzqKqZKvdpKuc147dw2N9d')throw Error('Mainnet required');}
async function stake(address){
 const cfg=(await rpc('getAccountInfo',[CONFIG,{encoding:'base64',commitment:'finalized'}])).value;
 if(!cfg)throw Error('Staking config unavailable');
 const accounts=await rpc('getProgramAccounts',[PROGRAM,{encoding:'base64',commitment:'finalized',filters:[{memcmp:{offset:0,bytes:encode(Buffer.from([102,53,163,107,9,138,87,153]))}},{memcmp:{offset:41,bytes:address}}]}]);
 return stakeUnits(cfg,accounts,address);
}
async function tokenAccount(address,required=0n){
 const accounts=(await rpc('getTokenAccountsByOwner',[address,{mint:MINT},{encoding:'jsonParsed',commitment:'finalized'}])).value;
 const valid=accounts.filter(a=>a.account.owner===TOKEN&&a.account.data.parsed.info.owner===address&&a.account.data.parsed.info.mint===MINT&&a.account.data.parsed.info.state==='initialized'&&a.account.data.parsed.info.tokenAmount.decimals===6&&BigInt(a.account.data.parsed.info.tokenAmount.amount)>=required);
 valid.sort((a,b)=>BigInt(a.account.data.parsed.info.tokenAmount.amount)>BigInt(b.account.data.parsed.info.tokenAmount.amount)?-1:1);
 if(!valid.length)throw Error(required?'Insufficient liquid SKR in a usable token account':'Recipient SKR token account must be initialized before checkout');
 return valid[0].pubkey;
}
function respond(res,status,data){res.writeHead(status,{'Content-Type':'application/json','Cache-Control':'no-store','X-Content-Type-Options':'nosniff'});res.end(JSON.stringify(data));}
async function body(req){
 let bytes=0,parts=[];
 for await(const p of req){bytes+=p.length;if(bytes>8192)throw Error('Request too large');parts.push(p);}
 return JSON.parse(Buffer.concat(parts).toString('utf8')||'{}');
}
const limits=new Map();
function limit(ip){
 const t=Date.now(),v=limits.get(ip);
 if(!v||v.until<t){limits.set(ip,{until:t+60000,count:1});}else{if(++v.count>30)throw Error('Too many requests');}
 if(limits.size>10000)for(const [k,v]of limits)if(v.until<t)limits.delete(k);
}
const server=http.createServer(async(req,res)=>{
 try{
  limit(req.socket.remoteAddress);
  const url=new URL(req.url,'http://localhost');const c=config();
  if(req.method==='GET'&&url.pathname==='/v1/config')return respond(res,200,{...c,mint:MINT,paymentVerification:'finalized',plans:[{id:'premium-30',days:30,priceSkr:c.premiumPriceSkr}],futurePlans:[90,180,365]});
  if(req.method==='GET'&&url.pathname==='/health')return respond(res,200,{ok:true,paymentsEnabled:c.premiumAvailable});
  if(req.method==='GET'&&url.pathname==='/v1/wallet'){
   const address=wallet(url.searchParams.get('address'));await mainnet();
   let amount=null;try{amount=await stake(address);}catch{}
   const old=ledger.data.premiums[address];
   const premium=premiumStatus(old,amount,now(),units(String(c.minimumStakeSkr)));
   if(old&&amount!==null){ledger.data.premiums[address]=premium;ledger.save();}
   return respond(res,200,{wallet:address,checkedAt:now(),stakedSkr:amount===null?null:Number(amount)/1e6,premium,inventory:ledger.data.inventory[address]??[]});
  }
  if(req.method==='POST'&&url.pathname==='/v1/orders'){
   const b=await body(req),address=wallet(b.wallet),item=String(b.item);
   await mainnet();
   let price;
   if(item==='premium'){
    if(!c.premiumAvailable)throw Error('Premium unavailable');
    if(await stake(address)<units(String(c.minimumStakeSkr)))throw Error('Minimum active staking requirement not met');
    price=c.premiumPriceSkr;
   }else{
    const product=c.items.find(i=>i.id===item);
    if(!product?.available||!product.priceSkr)throw Error('Item not available for purchase');
    if((ledger.data.inventory[address]??[]).some(i=>i.item===item))throw Error('Item already owned');
    price=product.priceSkr;
   }
   const t=now();
   for(const [id,o]of Object.entries(ledger.data.orders))if(!o.signature&&o.expiresAt<t-86400)delete ledger.data.orders[id];
   const pending=Object.values(ledger.data.orders).filter(o=>!o.signature&&o.expiresAt>=t);
   if(pending.length>=500||pending.filter(o=>o.wallet===address).length>=3)throw Error('Too many pending orders; retry later');
   const amountUnits=units(price).toString();
   const [source,destination,blockhash]=await Promise.all([tokenAccount(address,BigInt(amountUnits)),tokenAccount(c.recipient),rpc('getLatestBlockhash',[{commitment:'finalized'}])]);
   if(source===destination)throw Error('Payer cannot be the receiving account');
   const order={id:randomUUID(),wallet:address,item,priceSkr:String(price),amountUnits,recipient:c.recipient,source,destination,mint:MINT,decimals:6,createdAt:t,expiresAt:t+600,blockhash:blockhash.value.blockhash,lastValidBlockHeight:blockhash.value.lastValidBlockHeight,minimumStakeSkr:c.minimumStakeSkr};
   ledger.data.orders[order.id]=order;ledger.save();return respond(res,200,order);
  }
  if(req.method==='POST'&&url.pathname==='/v1/verify'){
   const b=await body(req),order=ledger.data.orders[String(b.orderId)];
   if(!order)throw Error('Order not found');
   if(decode(b.signature).length!==64)throw Error('Invalid transaction signature');
   if(order.signature){if(order.signature!==b.signature)throw Error('Order already paid');return respond(res,200,{verified:true,wallet:order.wallet,item:order.item});}
   await mainnet();
   const tx=await rpc('getTransaction',[b.signature,{encoding:'jsonParsed',commitment:'finalized',maxSupportedTransactionVersion:0}]);
   const paidAt=validatePayment(tx,order,b.signature);
   // Payment order already required a verified stake before signing. Recheck fresh stake for immediate entitlement state.
   let currentStake=null;if(order.item==='premium')try{currentStake=await stake(order.wallet);}catch{}
   ledger.commit(order,b.signature,paidAt);
   if(order.item==='premium'){
    const p=premiumStatus(ledger.data.premiums[order.wallet],currentStake,now(),units(String(c.minimumStakeSkr)));
    ledger.data.premiums[order.wallet]=p;ledger.save();
   }
   return respond(res,200,{verified:true,wallet:order.wallet,item:order.item});
  }
  respond(res,404,{error:'Not found'});
 }catch(e){respond(res,400,{error:e.message??'Request failed'});}
});
server.requestTimeout=25000;server.headersTimeout=10000;
server.listen(Number(process.env.PORT??8087),process.env.HOST??'127.0.0.1',()=>console.log('Walletkin commerce listening; use HTTPS reverse proxy for Android.'));

