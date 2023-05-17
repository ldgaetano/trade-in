# trade-in: Card Value Mapping Issuance Contract - v1.0.0

## Contract

- [ErgoScript](ergoscript/card_value_mapping_issuance.es)

## Documentation

### Description
This contract guards the Card Value Mapping Issuance box, which holds the minted singleton token to uniquely identify the Card Value Mapping box.

### Box Contents
Tokens: Coll[(Coll[Byte], Long)]
1. (CardValueMappingSingletonTokenId, 1)

Registers:
- R4: Coll[Byte] CardValueMappingSingletonTokenName
- R5: Coll[Byte] CardValueMappingSingletonTokenDescription
- R6: Coll[Byte] CardValueMappingSingletonTokenDecimals

### Relevant Transactions
1. Card Value Mapping Box Creation Tx
- Inputs: CardValueMappingIssuance
- DataInputs: None
- Outputs: CardValueMapping, MinerFee
- Context Variables: None

### Compile Time Constants ($)
- $CardValueMappingContractBytes: Coll[Byte]
- $SafeStorageRentValue: Long
- $DevPK: SigmaProp
- $MinerFee: Long

### Context Variables (#)
- None