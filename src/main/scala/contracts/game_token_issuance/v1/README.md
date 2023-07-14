# Trade-In: Game Token Issuance Contract - v1.0.0

## Contract

- [ErgoScript](ergoscript/game_token_issuance.es)

## Documentation

### Description
This contract guards the Game Token Issuance box, which holds the minted game tokens.

### Box Contents
Tokens: Coll[(Coll[Byte], Long)]
1. (GameTokenId, GameTokenAmount)

Registers:
- R4: Coll[Byte] GameTokenName
- R5: Coll[Byte] GameTokenDescription
- R6: Coll[Byte] GameTokenDecimals

### Relevant Transactions
1. Game LP Box Creation Tx
- Inputs: GameLPIssuance, GameTokenIssuance
- DataInputs: None
- Outputs: GameLP, MinerFee
- Context Variables: None

### Compile Time Constants ($)
- $GameLPIssuanceContractBytes: Coll[Byte]
- $GameLPContractBytes: Coll[Byte]
- $SafeStorageRentValue: Long
- $DevPK: SigmaProp
- $DevAddress: Coll[Byte]
- $MinerFee: Long

### Context Variables (_)
- None