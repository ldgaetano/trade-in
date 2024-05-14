{

    // ===== Contract Information ===== //
    // Name: Card Value Mapping
    // Description: This contract guards the Card Value Mapping box. For each card in a set, there exists
    //              a corresponding card-value-mapping box that holds information about the card token and
    //              its maximum possible value.
    // Version: 1.0.0
    // Author: Luca D'Angelo (ldgaetano@protonmail.com)

    // ===== Box Contents ===== //
    // Tokens: Coll[(Coll[Byte], Long)]
    // 1. (CardValueMappingTokenId, 1)
    // Registers:
    // R4: Coll[Byte] GameTokenId
    // R5: Coll[Byte] CardSetCollectionTokenId
    // R6: Coll[Byte] CardTokenId
    // R7: Long       Card Value

    // ===== Relevant Transactions ===== //
    // 1. Trade-In Tx
    // Inputs: GameLP, PlayerProxy
    // DataInputs: CardValueMapping
    // Outputs: GameLP, PlayerPK, DevAddress, TxOperator, MinerFee
    // Context Variables: None
    // 2. Storage Rent Top Up Tx
    // Inputs: CardValueMapping, StorageRentTopUp
    // DataInputs: None
    // Outputs: CardValueMapping, MinerFee
    // Context Variables: TxType
    
    // ===== Compile Time Constants ($) ===== //
    // $DevPKGE: GroupElement

    // ===== Context Variables (_) ===== //
    // _TxType: Byte

    // ===== Relevant Variables ===== //
    val devPK: SigmaProp = proveDlog($DevPKGE)
    val _TxType: Byte = getVar[Byte](0).get

    // ===== Trade-In Tx ===== //
    if (_TxType == 2) {

        val validStorageRentTopUpTx: Boolean = {

            // Outputs
            val cardValueMappingBoxOUT: Box = OUTPUTS(0)

            val validTopUp: Boolean = (cardValueMappingBoxOUT.value > SELF.value)

            val validSelfRecreation: Boolean = {

                allOf(Coll(
                    (cardValueMappingBoxOUT.tokens == SELF.tokens),
                    (cardValueMappingBoxOUT.propositionBytes == SELF.propositionBytes),
                    (cardValueMappingBoxOUT.R4[Coll[Byte]].get == SELF.R4[Coll[Byte]].get),
                    (cardValueMappingBoxOUT.R5[Coll[Byte]].get == SELF.R5[Coll[Byte]].get),
                    (cardValueMappingBoxOUT.R6[Coll[Byte]].get == SELF.R6[Coll[Byte]].get),
                    (cardValueMappingBoxOUT.R7[Long].get == SELF.R7[Long].get)
                ))

            }

            allOf(Coll(
                validTopUp,
                validSelfRecreation
            ))

        }

        sigmaProp(validStorageRentTopUpTx) && devPK

    } else {
        sigmaProp(false) // This box cannot be spent.
    }

}