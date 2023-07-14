package configs.setup_config.settings

case class TradeInSettingsConfig(
                               gameTokenMinting: TradeInGameTokenMintingConfig,
                               gameLPBoxCreation: TradeInGameLPBoxCreationConfig,
                               cardValueMappingBoxCreation: TradeInCardValueMappingBoxCreationConfig,
                               devAddress: String,
                               protocolPeriodInYears: Long,
                               devFeeInGameTokenPercentage: Double,
                               txOperatorFeeInGameTokenPercentage: Double,
                               minerFeeInNanoERG: Long
                             )
{}