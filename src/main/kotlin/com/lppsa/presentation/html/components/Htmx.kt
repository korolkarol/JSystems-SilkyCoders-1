package com.lppsa.presentation.html.components

import kotlinx.html.CommonAttributeGroupFacade

object Htmx {
    fun CommonAttributeGroupFacade.hxPost(url: String) { attributes["hx-post"] = url }
    fun CommonAttributeGroupFacade.hxGet(url: String) { attributes["hx-get"] = url }
    fun CommonAttributeGroupFacade.hxTarget(selector: String) { attributes["hx-target"] = selector }
    fun CommonAttributeGroupFacade.hxSwap(strategy: String) { attributes["hx-swap"] = strategy }
    fun CommonAttributeGroupFacade.hxTrigger(trigger: String) { attributes["hx-trigger"] = trigger }
    fun CommonAttributeGroupFacade.hxIndicator(selector: String) { attributes["hx-indicator"] = selector }
    fun CommonAttributeGroupFacade.hxDisabledElt(selector: String) { attributes["hx-disabled-elt"] = selector }
    fun CommonAttributeGroupFacade.hxEncoding(encoding: String) { attributes["hx-encoding"] = encoding }
    fun CommonAttributeGroupFacade.hxExt(vararg extensions: String) { attributes["hx-ext"] = extensions.joinToString(",") }
    fun CommonAttributeGroupFacade.sseConnect(url: String) { attributes["sse-connect"] = url }
    fun CommonAttributeGroupFacade.sseSwap(eventName: String) { attributes["sse-swap"] = eventName }
}
