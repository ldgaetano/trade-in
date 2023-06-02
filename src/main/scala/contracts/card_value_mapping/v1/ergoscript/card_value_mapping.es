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
    
    // ===== Compile Time Constants ($) ===== //
    // None

    // ===== Context Variables (_) ===== //
    // None

    // ===== Relevant Variables ===== //
    // None

    // ===== Trade-In Tx ===== //
    sigmaProp(false) // This box cannot be spent.

}