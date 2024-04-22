{

    // ===== Contract Information ===== //
    // Name: Player Proxy
    // Description: This contract guards the Player Proxy box, which holds the player's card token.
    //              The logic of this contract determines how many game tokens the player will receive
    //              based on the card they hold.
    // Version: 1.0.0
    // Author: Luca D'Angelo (ldgaetano@protonmail.com)

    // ===== Box Contents ===== //
    // Tokens: Coll[(Coll[Byte], Long)]
    // 1. (CardTokenId, 1)
    // Registers:
    // R4: GroupElement     PlayerPK
    // R5: Coll[Byte]       GameLPSingletonTokenId
    // R6: Coll[Byte]       GameTokenId
    // R7: Long             MinerFee

    // ===== Relevant Transactions ===== //
    // 1. Trade-In Tx
    // Inputs: GameLP, PlayerProxy
    // DataInputs: CardValueMapping
    // Outputs: GameLP, PlayerPK, DevAddress, TxOperator, MinerFee
    // Context Variables: CardTokenIssuerBox, CardSetCollectionIssuerBox
    
    // ===== Compile Time Constants ($) ===== //
    // None

    // ===== Context Variables (_) ===== //
    // None

    // ===== Relevant Variables ===== //
    val cardTokenId: Coll[Byte]                     = SELF.tokens(0)._1
    val playerPKGE: GroupElement                    = SELF.R4[GroupElement]get
    val playerPK: SigmaProp                         = proveDlog(playerPKGE)
    val gameLPSingletonTokenId: Coll[Byte]          = SELF.R5[Coll[Byte]].get
    val gameTokenId: Coll[Byte]                     = SELF.R6[Coll[Byte]].get
    val minerFee: Long                              = SELF.R7[Long].get
    val minerFeeErgoTreeBytesHash: Coll[Byte]       = fromBase16("e540cceffd3b8dd0f401193576cc413467039695969427df94454193dddfb375")
    val isFees: Boolean = !(OUTPUTS.size == 3)

    // ===== Trade-In Tx ===== //
    val validTradeInTx: Boolean = {

        // Inputs
        val gameLPBoxIN: Box        = INPUTS(0)
        
        // Outputs
        val gameLPBoxOUT: Box       = OUTPUTS(0)
        val playerPKBoxOUT: Box     = if (isFees) OUTPUTS(3) else OUTPUTS(1)
        val minerFeeBoxOUT: Box     = if (isFees) OUTPUTS(4) else OUTPUTS(2)

        val validGameLPBoxIN: Boolean = {

            val validGameLPBoxSingletonToken: Boolean = (gameLPBoxIN.tokens(0) == (gameLPSingletonTokenId, 1L))
            val validGameTokenId: Boolean = if (gameLPBoxIN.tokens.size >= 3) (gameLPBoxIN.tokens(1)._1 == gameTokenId) else false

            allOf(Coll(
                validGameLPBoxSingletonToken,
                validGameTokenId
            ))

        }

        val validPlayerPKBoxOUT: Boolean = {

            val validValue: Boolean = (playerPKBoxOUT.value == SELF.value - minerFee)
            val validContract: Boolean = (playerPKBoxOUT.propositionBytes == playerPK.propBytes)

            val validGameTokenTransfer: Boolean = {

                val delta: Long = (gameLPBoxIN.tokens(1)._2 - gameLPBoxOUT.tokens(1)._2)
                val playerAmount: Long = if (isFees) delta - (OUTPUTS(1).tokens(0)._2 + OUTPUTS(2).tokens(0)._2) else delta         
                
                allOf(Coll(
                    (playerPKBoxOUT.tokens(0) == (gameTokenId, playerAmount))
                ))

            }

            allOf(Coll(
                validValue,
                validContract,
                validGameTokens
            ))

        }

        val validMinerFee: Boolean = {

            allOf(Coll(
                (minerFeeBoxOUT.value == minerFee),
                (blake2b256(minerFeeBoxOUT.propositionBytes) == minerFeeErgoTreeBytesHash)
            ))

        }

        allOf(Coll(
            validGameLPBoxIN,
            validPlayerPKBoxOUT,
            validMiner
        ))

    }

    sigmaProp(validTradeInTx)

}