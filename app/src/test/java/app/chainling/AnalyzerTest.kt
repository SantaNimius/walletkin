package app.chainling

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class AnalyzerTest {
    private val address="11111111111111111111111111111111"
    private fun transaction(instructions:String="[]",inner:String="[]",post:Long=9000,err:String="null",time:String="42",preTokens:String="[]",postTokens:String="[]")=JSONObject("""{
      "blockTime":$time,"transaction":{"message":{"accountKeys":[{"pubkey":"$address","signer":true}],"instructions":$instructions}},
      "meta":{"err":$err,"fee":1000,"preBalances":[10000],"postBalances":[$post],"innerInstructions":$inner,"preTokenBalances":$preTokens,"postTokenBalances":$postTokens}
    }""")
    private fun parse(j:JSONObject)=Analyzer(Rpc("https://api.mainnet-beta.solana.com")).parseTransaction(address,"test",j)
    @Test fun feeOnlyDoesNotBecomeGivingHands(){assertFalse(parse(transaction()).outgoing)}
    @Test fun directSystemTransferIsEvidence(){val instruction="""[{"programId":"11111111111111111111111111111111","parsed":{"type":"transfer","info":{"source":"$address","destination":"other","lamports":1}}}]""";assertTrue(parse(transaction(instructions=instruction,post=8999)).outgoing)}
    @Test fun jupiterInnerInvocationIsRecognized(){val inner="""[{"index":0,"instructions":[{"programId":"${Evolution.JUPITER}"}]}]""";assertTrue(Evolution.JUPITER in parse(transaction(inner=inner)).programs)}
    @Test fun failedTransactionCannotBecomeMemory(){val e=parse(transaction(err="{\"InstructionError\":[0,\"InvalidArgument\"]}"));assertFalse(e.success);val s=Snapshot(address,0.0,null,null,null,0,listOf(e),0,emptyList(),false);assertTrue(Evolution.derive(s).memories.isEmpty())}
    @Test fun missingTimestampStaysNull(){assertNull(parse(transaction(time="null")).time)}
    @Test fun unchangedSkrTokenBalanceIsNotHistory(){val token="""[{"accountIndex":1,"mint":"${Analyzer.SKR_MINT}","owner":"$address","uiTokenAmount":{"amount":"100"}}]""";assertFalse(parse(transaction(preTokens=token,postTokens=token)).skr)}
    @Test fun changedSkrTokenBalanceIsHistory(){val token="""[{"accountIndex":1,"mint":"${Analyzer.SKR_MINT}","owner":"$address","uiTokenAmount":{"amount":"100"}}]""";assertTrue(parse(transaction(postTokens=token)).skr)}
}
