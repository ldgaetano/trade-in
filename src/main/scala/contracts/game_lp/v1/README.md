# Trade-In: Game LP Contract - v1.0.0

## Contract

- [ErgoScript](ergoscript/game_lp.es)

## Documentation

### Description
This contract guards the Game LP box, which holds the game tokens. This contract ensures that tokens can be withdrawn from the LP only when the Card Value Mapping and Player Proxy boxes are inputs. The Player Proxy contract determines the correct value that must be withdrawn from the LP. This ensures that the protocol stays modular by enabling the structure of the Game LP contract to remain constant, while only requiring the Player Proxy contract to be modified.

### Box Contents
Tokens: Coll[(Coll[Byte], Long)]
1. (GameLPSingletonTokenId, 1)
2. (GameTokenId, GameTokenAmount)
3. (CardValueMappingToken, 1)

Registers:
- R4: (Coll[(Long, Long)], Coll[Byte]) (Coll(DevFee, TradeInFee, TxOperatorFee), GameTokenId)
- R5: Long       EmissionInterval
- R6: Long       EmissionReductionFactorMultiplier
- R7: Long       EmissionReductionFactor  
- R8: Long       CardTokenBurnCount
- R9: Long       CardTokenBurnTotal

### Relevant Transactions
1. Trade-In Tx
- Inputs: GameLP, PlayerProxy
- DataInputs: CardValueMapping
- Outputs: GameLP, PlayerPK, DevAddress, TxOperator, MinerFee
- Context Variables: None

2. Card-Value-Mapping Box Creation Tx
- Inputs: GameLP, CardValueMappingIssuance
- DataInputs: None
- Outputs: GameLP, CardValueMapping1, ... , CardValueMappingN, MinerFee
- Context Variables: None

### Compile Time Constants ($)
- $CardValueMappingContractBytes: Coll[Byte]
- $PlayerProxyContractBytes: Coll[Byte]
- $DevPK: SigmaProp
- $DevAddress: Coll[Byte]
- $TradeInFeeAddress: SigmaProp
- $MinBoxValue: Long
- $SetCreationMutliSigThreshold: Int
- $SetCreationMultiSigAddresses: Coll[SigmaProp]

### Context Variables (_)
- _TransactionType: Byte