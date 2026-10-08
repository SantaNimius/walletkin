import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync,rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { MINT,TOKEN,MEMO,validatePayment,premiumStatus,units,Ledger } from './commerce.mjs';
const order={id:'one',wallet:'payer',recipient:'merchant',source:'source',destination:'dest',amountUnits:'100000000',createdAt:1000,expiresAt:1600,item:'premium'};
function tx(){return {blockTime:1100,meta:{err:null,preTokenBalances:[{accountIndex:2,mint:MINT,owner:'merchant',uiTokenAmount:{amount:'10'}}],postTokenBalances:[{accountIndex:2,mint:MINT,owner:'merchant',uiTokenAmount:{amount:'100000010'}}]},transaction:{signatures:['sig'],message:{accountKeys:[{pubkey:'payer',signer:true},{pubkey:'source'},{pubkey:'dest'}],instructions:[{programId:MEMO,parsed:'walletkin:one'},{programId:TOKEN,parsed:{type:'transferChecked',info:{mint:MINT,authority:'payer',source:'source',destination:'dest',tokenAmount:{amount:'100000000',decimals:6}}}}]}}};}
test('exact finalized payment accepted',()=>assert.equal(validatePayment(tx(),order,'sig'),1100));
test('wrong mint, receiver, payer, amount, memo, failed and stale transactions rejected',()=>{
 for(const change of [
  t=>t.meta.err={InstructionError:[0,1]},
  t=>t.blockTime=99999,
  t=>t.transaction.message.accountKeys[0].signer=false,
  t=>t.transaction.message.instructions[0].parsed='walletkin:other',
  t=>t.transaction.message.instructions[1].parsed.info.mint='fake',
  t=>t.transaction.message.instructions[1].parsed.info.destination='attacker',
  t=>t.transaction.message.instructions[1].parsed.info.authority='other',
  t=>t.transaction.message.instructions[1].parsed.info.tokenAmount.amount='1',
  t=>t.meta.postTokenBalances[0].uiTokenAmount.amount='1',
 ]){const t=tx();change(t);assert.throws(()=>validatePayment(t,order,'sig'));}
});
test('partial unstake triggers grace and restored stake returns active',()=>{
 const p={expiresAt:999999};const grace=premiumStatus(p,90000000n,1000);
 assert.equal(grace.status,'GRACE_PERIOD');assert.equal(grace.graceUntil,605800);
 assert.equal(premiumStatus(grace,100000000n,1100).status,'ACTIVE');
 assert.equal(premiumStatus(grace,90000000n,605800).status,'INACTIVE');
});
test('expiry outranks grace, unknown preserves existing grace, configurable threshold',()=>{
 const p={expiresAt:1100};
 assert.equal(premiumStatus(p,0n,1000).graceUntil,1100);
 assert.equal(premiumStatus(p,100000000n,1100).status,'EXPIRED');
 const grace=premiumStatus({expiresAt:999999},0n,1000);
 const unknown=premiumStatus(grace,null,2000);assert.equal(unknown.status,'UNKNOWN');assert.equal(unknown.graceUntil,grace.graceUntil);
 assert.equal(premiumStatus(null,150000000n,1000,200000000n).status,'LOCKED');
});
test('atomic ledger: replay gives no second entitlement; restart preserves payments',()=>{
 const path=mkdtempSync(join(tmpdir(),'walletkin-commerce-'));
 try{
 const l=new Ledger(path);l.data.orders.one=order;l.save();l.commit(order,'sig',1100);
 const expiry=l.data.premiums.payer.expiresAt;l.commit(order,'sig',1100);assert.equal(l.data.premiums.payer.expiresAt,expiry);
 assert.throws(()=>l.commit({...order,id:'two'},'sig',1200));
 assert.equal(new Ledger(path).data.premiums.payer.expiresAt,expiry);
 }finally{rmSync(path,{recursive:true});}
});
test('prices use precise integer units',()=>{assert.equal(units('100'),100000000n);assert.equal(units('0.000001'),1n);assert.throws(()=>units('0'));assert.throws(()=>units('1.0000001'));});

