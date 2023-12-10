{

    // ===== Contract Information ===== //
    // Name: Game LP Issuance
    // Description: This contract guards the Game LP Issuance box, which holds the minted LP box singleton token. 
    //              This singleton token will be used as the unique identifier of the Game LP box. 
    //              This is required because as the LP box is recreated during withdrawls, the box id will change.
    // Version: 1.0.0
    // Author: Luca D'Angelo (ldgaetano@protonmail.com)

    // ===== Box Contents ===== //
    // Tokens: Coll[(Coll[Byte], Long)]
    // 1. (GameLPSingletonTokenId, 1)
    // Registers:
    // R4: Coll[Byte] GameLPSingletonTokenName
    // R5: Coll[Byte] GameLPSingletonTokenDescription
    // R6: Coll[Byte] GameLPSingletonTokenDecimals

    // ===== Relevant Transactions ===== //
    // 1. Game LP Box Creation Tx
    // Inputs: GameLPIssuance, GameTokenIssuance
    // DataInputs: None
    // Outputs: GameLP, MinerFee
    // Context Variables: None
    
    // ===== Compile Time Constants ($) ===== //
    // $GameLPContractBytes: Coll[Byte]
    // $DevPK: SigmaProp

    // ===== Context Variables (_) ===== //
    // None

    // ===== Relevant Variables ===== //
    // None

    // ===== Game LP Box Creation Tx ===== //
    val validGameLPBoxCreationTx: Boolean = {

        // Inputs
        val gameTokenIssuanceBoxIN: Box = INPUTS(1)
        
        // Outputs    
        val gameLPBoxOUT: Box = OUTPUTS(0)
        val minerFeeBoxOUT: Box = OUTPUTS(1)

        val validGameLPBoxOUT: Boolean = {

            val validValue: Boolean = (SELF.value == gameLPBoxOUT.value)
            val validContract: Boolean = ($GameLPContractBytes == gameLPBoxOUT.propositionBytes)
            val validSingletonToken: Boolean = ((gameLPBoxOUT.tokens(0)._1, 1L) == SELF.tokens(0))

            allOf(Coll(
                validValue,
                validContract,
                validSingletonToken
            ))
            
        }

        val validOutputSize: Boolean = (OUTPUTS.size == 2)

        allOf(Coll(
            validGameLPBoxOUT,
            validOutputSize
        ))

    }

    sigmaProp(validGameLPBoxCreationTx) && $DevPK

}