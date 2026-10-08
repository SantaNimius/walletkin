import { readFileSync, writeFileSync, renameSync, mkdirSync } from 'node:fs';
import { resolve } from 'node:path';
export const MINT='SKRbvo6Gf7GondiT3BbTfuRDPqLWei4j2Qy2NPGZhW3';
export const TOKEN='TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA';
export const MEMO='MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr';
export const PROGRAM='SKRskrmtL83pcL4YqLWt6iPefDqwXQWHSw9S9vz94BZ';
export const CONFIG='4HQy82s9CHTv1GsYKnANHMiHfhcqesYkK6sB3RDSYyqw';
const alphabet='123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz';
export function decode(s){
 if(typeof s!=='string'||s.length>100||!s.length)throw Error('Invalid base58');
 let n=0n;for(const c of s){const i=alphabet.indexOf(c);if(i<0)throw Error('Invalid base58');n=n*58n+BigInt(i);}
 const a=[];while(n){a.unshift(Number(n%256n));n/=256n;}
 return Buffer.from([...Array(s.match(/^1*/)[0].length).fill(0),...a]);
}
export function encode(bytes){
 let n=BigInt('0x'+(Buffer.from(bytes).toString('hex')||'00')),out='';
 while(n){out=alphabet[Number(n%58n)]+out;n/=58n;}
 for(const b of bytes){if(b!==0)break;out='1'+out;}return out;
}
export function wallet(s){if(decode(s).length!==32)throw Error('Invalid wallet');return s;}
export function units(s){
 if(!/^(0|[1-9]\d{0,9})(\.\d{1,6})?$/.test(String(s)))throw Error('Invalid SKR price');
 const [a,b='']=String(s).split('.');const n=BigInt(a)*1000000n+BigInt(b.padEnd(6,'0'));
 if(n<=0n)throw Error('Price must be positive');return n;
}
function u128(b,o){if(b.length<o+16)throw Error('Invalid stake account');return b.readBigUInt64LE(o)+(b.readBigUInt64LE(o+8)<<64n);}
export function stakeUnits(config,accounts,address){
 if(config.owner!==PROGRAM)throw Error('Invalid staking owner');
 const cfg=Buffer.from(config.data[0],'base64');
 if(cfg.length<153||!cfg.subarray(0,8).equals(Buffer.from([238,151,43,3,11,151,63,176]))||encode(cfg.subarray(41,73))!==MINT)throw Error('Invalid staking config');
 const price=u128(cfg,137);if(price===0n)throw Error('Invalid share price');
 let shares=0n;
 for(const {account} of accounts){
  const b=Buffer.from(account.data[0],'base64');
  if(account.owner!==PROGRAM||b.length<169||!b.subarray(0,8).equals(Buffer.from([102,53,163,107,9,138,87,153]))||encode(b.subarray(9,41))!==CONFIG||encode(b.subarray(41,73))!==address)throw Error('Invalid stake identity');
  shares+=u128(b,105);
 }
 return shares*price/1000000000n; // 1e9 share-price scale; result in mint base units (6 decimals)
}
export function validatePayment(tx,order,signature){
 if(!tx||!tx.meta||tx.meta.err!==null)throw Error('Payment not finalized successfully');
 if(!tx.transaction.signatures.includes(signature))throw Error('Signature mismatch');
 if(!Number.isInteger(tx.blockTime)||tx.blockTime<order.createdAt-5||tx.blockTime>order.expiresAt)throw Error('Payment outside order window');
 const msg=tx.transaction.message;
 if(!msg.accountKeys.some(k=>k.pubkey===order.wallet&&k.signer))throw Error('Wallet did not sign');
 if(!msg.instructions.some(i=>i.programId===MEMO&&i.parsed==='walletkin:'+order.id))throw Error('Order memo missing');
 const transfers=msg.instructions.filter(i=>i.programId===TOKEN&&i.parsed?.type==='transferChecked');
 if(transfers.length!==1)throw Error('Expected one checked SKR transfer');
 const info=transfers[0].parsed.info;
 if(info.mint!==MINT||info.authority!==order.wallet||info.destination!==order.destination||info.source!==order.source||info.tokenAmount.decimals!==6||info.tokenAmount.amount!==order.amountUnits)throw Error('Payment details do not match order');
 const index=msg.accountKeys.findIndex(k=>k.pubkey===order.destination);
 const balances=(list)=>list?.find(b=>b.accountIndex===index&&b.mint===MINT&&b.owner===order.recipient)?.uiTokenAmount.amount;
 const after=balances(tx.meta.postTokenBalances),before=balances(tx.meta.preTokenBalances);
 if(after===undefined||BigInt(after)-BigInt(before??'0')<BigInt(order.amountUnits))throw Error('Recipient did not receive SKR');
 return tx.blockTime;
}
export function premiumStatus(record,stake,now,minimumStake=100000000n){
 if(!record)return {status:stake===null?'UNKNOWN':stake>=minimumStake?'ELIGIBLE':'LOCKED'};
 if(now>=record.expiresAt)return {...record,status:'EXPIRED'};
 if(stake===null)return {...record,status:'UNKNOWN'}; // never invent unstake or reset grace on RPC failure
 if(stake>=minimumStake)return {...record,graceUntil:null,status:'ACTIVE'};
 const graceUntil=record.graceUntil??Math.min(now+7*86400,record.expiresAt);
 return {...record,graceUntil,status:now<graceUntil?'GRACE_PERIOD':'INACTIVE'};
}
export class Ledger {
 constructor(directory){mkdirSync(directory,{recursive:true});this.path=resolve(directory,'ledger.json');try{this.data=JSON.parse(readFileSync(this.path,'utf8'));}catch(e){if(e.code!=='ENOENT')throw e;this.data={orders:{},signatures:{},premiums:{},inventory:{}};}}
 save(){const tmp=this.path+'.tmp';writeFileSync(tmp,JSON.stringify(this.data),{mode:0o600});renameSync(tmp,this.path);}
 commit(order,signature,paidAt){
  const existing=this.data.orders[order.id];
  if(existing?.signature){if(existing.signature!==signature)throw Error('Order already paid');return;}
  if(this.data.signatures[signature])throw Error('Payment already used');
  this.data.orders[order.id]={...order,signature,paidAt};this.data.signatures[signature]=order.id;
  if(order.item==='premium'){
   const old=this.data.premiums[order.wallet];
   this.data.premiums[order.wallet]={startedAt:paidAt,expiresAt:Math.max(old?.expiresAt??0,paidAt)+30*86400,graceUntil:null,lastPaymentSignature:signature};
  }else{
   const list=this.data.inventory[order.wallet]??[];
   if(!list.some(i=>i.item===order.item))list.push({item:order.item,signature,purchasedAt:paidAt});
   this.data.inventory[order.wallet]=list;
  }
  this.save();
 }
}


