# trade-in: Card Value Mapping Contract - v1.0.0

## Contract

- [ErgoScript](ergoscript/card_value_mapping.es)

## Documentation

### Description
This contract guards the Card Value Mapping box. For each card in a set, there exists a corresponding card-value-mapping box that holds information about the card token and its maximum possible value.

### Box Contents
Tokens: Coll[(Coll[Byte], Long)]
1. (CardValueMappingTokenId, 1)

Registers:
- R4: Coll[Byte] GameTokenId
- R5: Coll[Byte] CardSetCollectionTokenId
- R6: Coll[Byte] CardTokenId
- R7: Long       CardValue
### Relevant Transactions
1. Trade-In Tx
- Inputs: GameLP, PlayerProxy
- DataInputs: CardValueMapping
- Outputs: GameLP, PlayerPK, DevAddress, TxOperator, MinerFee
- Context Variables: None

### Compile Time Constants ($)
- None

### Context Variables (_)
- None