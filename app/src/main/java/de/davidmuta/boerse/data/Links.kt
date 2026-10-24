package de.davidmuta.boerse.data

/** Ein Link mit lesbarem Namen. */
data class LinkEintrag(val titel: String, val adresse: String)

/** Eine Gruppe von Links, z. B. „Morgenroutine“. */
data class LinkGruppe(val titel: String, val symbol: LinkSymbol, val links: List<LinkEintrag>)

enum class LinkSymbol { SONNE, KURS, PROZENT }

/** Die feste Linkliste des Auftraggebers (Stand 24.10.2026). */
object LinkListe {
    val gruppen: List<LinkGruppe> = listOf(
        LinkGruppe(
            titel = "Morgenroutine",
            symbol = LinkSymbol.SONNE,
            links = listOf(
                LinkEintrag("NYSE Advance/Decline (täglich)", "https://stockcharts.com/sc3/ui/?s=%24NYAD&p=d&b=5&g=0&id=p23301826123"),
                LinkEintrag("NYSE Advance/Decline (wöchentlich)", "https://stockcharts.com/sc3/ui/?s=%24NYADW&p=w&b=5&g=0&id=p52270302463"),
                LinkEintrag("Fear & Greed Index", "https://edition.cnn.com/markets/fear-and-greed"),
                LinkEintrag("Cboe Marktstatistik", "https://www.cboe.com/markets/us/options/market-statistics#current-stats?mkt=cone"),
                LinkEintrag("Momentum-Aktien", "https://www.barchart.com/stocks/momentum"),
                LinkEintrag("Branchen-Gruppen", "https://finviz.com/groups"),
                LinkEintrag("Sektoren-Karte", "https://finviz.com/map?t=sec"),
                LinkEintrag("AAII Stimmungsumfrage", "https://www.aaii.com/sentimentsurvey"),
                LinkEintrag("US-Aktien auf 52-Wochen-Hoch", "https://finviz.com/screener/stocks?v=111&f=geo_usa,ta_highlow52w_nh&ft=3&o=-marketcap"),
                LinkEintrag("Volchart", "https://volchart.io/"),
                LinkEintrag("VIX Saisonalität", "https://market-bulls.com/seasonal-tendencies-vix/"),
                LinkEintrag("Futures-Entwicklung", "https://finviz.com/futures/performance?v=17"),
                LinkEintrag("FedWatch (Zinserwartung)", "https://www.cmegroup.com/markets/interest-rates/cme-fedwatch-tool.html")
            )
        ),
        LinkGruppe(
            titel = "Trading",
            symbol = LinkSymbol.KURS,
            links = listOf(
                LinkEintrag("Fear & Greed Index", "https://edition.cnn.com/markets/fear-and-greed"),
                LinkEintrag("VIX Terminstrukturkurve", "https://volchart.io/"),
                LinkEintrag("OptionStrat", "https://optionstrat.com/")
            )
        ),
        LinkGruppe(
            titel = "Borrow-Fee's",
            symbol = LinkSymbol.PROZENT,
            links = listOf(
                LinkEintrag("VXX Borrow-Fee", "https://chartexchange.com/symbol/bats-vxx/borrow-fee/"),
                LinkEintrag("SPY Borrow-Fee", "https://chartexchange.com/symbol/nyse-spy/borrow-fee/"),
                LinkEintrag("USO Borrow-Fee", "https://chartexchange.com/symbol/nyse-uso/borrow-fee/"),
                LinkEintrag("TLT Borrow-Fee", "https://chartexchange.com/symbol/nasdaq-tlt/borrow-fee/")
            )
        )
    )
}
