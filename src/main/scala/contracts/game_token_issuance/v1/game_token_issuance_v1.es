{

    // ===== Contract Information ===== //
    // Name: Game Token Issuance
    // Description: This contract guards the Game Token Issuance box, which holds the minted game tokens.
    // Version: 1.0.0
    // Author: Luca D'Angelo (ldgaetano@protonmail.com)

    // ===== Box Contents ===== //
    // Tokens: Coll[(Coll[Byte], Long)]
    // 1. (GameTokenId, GameTokenAmount + 1)
    // Registers:
    // R4: Coll[Byte] GameTokenName
    // R5: Coll[Byte] GameTokenDescription
    // R6: Coll[Byte] GameTokenDecimals

    // ===== Relevant Transactions ===== //
    // 1. Game LP Box Creation Tx
    // Inputs: GameLPIssuance, GameTokenIssuance
    // DataInputs: None
    // Outputs: GameLP, MinerFee
    // Context Variables: None
    
    // ===== Compile Time Constants ($) ===== //
    // $GameLPIssuanceContractBytes: Coll[Byte]
    // $GameLPContractBytes: Coll[Byte]
    // $DevPKGE: GroupElement

    // ===== Context Variables (_) ===== //
    // None

    // ===== Relevant Variables ===== //
    val devPK: SigmaProp = proveDlog($DevPKGE)
    val minerFeeErgoTreeBytesHash: Coll[Byte] = fromBase16("e540cceffd3b8dd0f401193576cc413467039695969427df94454193dddfb375")

    // ===== Game LP Box Creation Tx ===== //
    val validGameLPBoxCreationTx: Boolean = {

        // Inputs
        val gameLPIssuanceBoxIN: Box = INPUTS(0)
        
        // Outputs    
        val gameLPBoxOUT: Box = OUTPUTS(0)

        val validGameLPIssuanceBoxIN: Boolean = {

            val validContract: Boolean = ($GameLPIssuanceContractBytes == gameLPIssuanceBoxIN.propositionBytes)

            allOf(Coll(
                validContract
            ))

        }

        val validGameLPBoxOUT: Boolean = {

            val validTokens: Boolean = (gameLPBoxOUT.tokens(1) == (SELF.tokens(0)))

            allOf(Coll(
                validTokens
            ))
            
        }

        val validMinerFee: Boolean = {

            allOf(Coll(
                (minerFeeBoxOUT.value == SELF.value),
                (blake2b256(minerFeeBoxOUT.propositionBytes) == minerFeeErgoTreeBytesHash)
            ))

        }

        allOf(Coll(
            validGameLPIssuanceBoxIN,
            validGameLPBoxOUT,
            validMinerFee
        ))

    }

    sigmaProp(validGameLPBoxCreationTx) && devPK

}