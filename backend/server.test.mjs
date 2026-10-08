import test from 'node:test';
import assert from 'node:assert/strict';
import { spawn } from 'node:child_process';
import { mkdtempSync,rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
test('HTTP config exposes agreed MVP terms, future plans are not sellable',async()=>{
 const directory=mkdtempSync(join(tmpdir(),'walletkin-http-'));
 const child=spawn(process.execPath,['server.mjs'],{cwd:new URL('.',import.meta.url),windowsHide:true,stdio:'pipe',env:{...process.env,HOST:'127.0.0.1',PORT:'18087',DATA_DIR:directory}});
 let diagnostic='';child.stderr.on('data',b=>diagnostic+=b);
 try{
  let config;
  for(let i=0;i<30;i++){
   try{const r=await fetch('http://127.0.0.1:18087/v1/config');config=await r.json();break;}catch{await new Promise(r=>setTimeout(r,100));}
  }
  assert.ok(config,diagnostic);assert.equal(config.premiumPriceSkr,'100');assert.equal(config.minimumStakeSkr,100);
  assert.equal(config.recipient,'6y8jfXZHHSYYaVLaEDnokW3MEKg1JfzA9S1Ld6zCv4SF');
  assert.deepEqual(config.futurePlans,[90,180,365]);assert.equal(config.plans.length,1);
  assert.ok(config.items.every(i=>!i.available&&i.priceSkr===null));
  const bad=await fetch('http://127.0.0.1:18087/v1/orders',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({wallet:'invalid0',item:'premium'})});
  assert.equal(bad.status,400);assert.match((await bad.json()).error,/base58|wallet/i);
 }finally{child.kill();await new Promise(r=>child.once('exit',r));rmSync(directory,{recursive:true});}
});

