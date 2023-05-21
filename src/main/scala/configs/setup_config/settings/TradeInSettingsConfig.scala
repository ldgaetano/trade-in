package configs.setup_config.settings

case class TradeInSettingsConfig(
                               gameTokenMinting: TradeInGameTokenMintingConfig,
                               gameLPBoxCreation: TradeInGameLPBoxCreationConfig,
                               devAddress: String,
                               protocolPeriodInYears: Long,
                               devFeeInGameToken: Long,
                               txOperatorFeeInGameToken: Long,
                               minerFeeInNanoERG: Long
                             )
