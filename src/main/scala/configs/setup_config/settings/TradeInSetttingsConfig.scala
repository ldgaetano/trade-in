package configs.setup_config.settings

case class TradeInSetttingsConfig(
                               gameTokenMinting: TradeInGameTokenMintingConfig,
                               devAddress: String,
                               devFeeInGameToken: Long,
                               txOperatorFeeInGameToken: Long,
                               minerFeeInNanoERG: Long
                             )
