package configs.setup_config.settings

case class TradeInSettingsConfig(
                               gameTokenMinting: TradeInGameTokenMintingConfig,
                               gameLPBoxCreation: TradeInGameLPBoxCreationConfig,
                               cardValueMappingBoxCreation: TradeInCardValueMappingBoxCreationConfig,
                               setCreationMultiSig: TradeInSetCreationMutliSigConfig,
                               devAddress: String,
                               devFeeInGameTokenPercentage: Double,
                               txOperatorFeeInNanoErg: Long,
                               minerFeeInNanoERG: Long
                             )
{}