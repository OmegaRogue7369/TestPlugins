package com.dineth.pmvhaven

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class PMVHaven : MainAPI() {

    override var mainUrl = "https://pmvhaven.com"
    override var name = "PMV Haven"
    override val supportedTypes = setOf(TvType.NSFW)
    override val hasMainPage = true
    override val hasSearch = true

    override fun mainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = app.get(mainUrl).document

        val videos = doc.select("article, .video-item, .post").mapNotNull {
            toSearchResponse(it)
        }

        return HomePageResponse(
            listOf(
                HomePageList(
                    name = "Latest PMVs",
                    list = videos,
                    isHorizontalImages = true
                )
            )
        )
    }

    private fun toSearchResponse(element: Element): SearchResponse? {
        val link = element.selectFirst("a")?.attr("href") ?: return null
        val title = element.selectFirst("img")?.attr("alt")
            ?: element.text().trim()

        val poster = element.selectFirst("img")?.attr("src")

        return newMovieSearchResponse(
            title,
            fixUrl(link),
            TvType.NSFW
        ) {
            this.posterUrl = fixUrlNull(poster)
        }
    }

    override fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=${query.replace(" ", "+")}"
        val doc = app.get(url).document

        return doc.select("article, .video-item, .post").mapNotNull {
            toSearchResponse(it)
        }
    }

    override fun load(url: String): LoadResponse {
        val doc = app.get(url).document

        val title = doc.selectFirst("h1")?.text() ?: "PMV Video"
