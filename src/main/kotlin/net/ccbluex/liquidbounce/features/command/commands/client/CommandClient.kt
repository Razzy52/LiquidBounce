/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2024 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.features.command.commands.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.config.ConfigSystem
import net.ccbluex.liquidbounce.features.command.builder.CommandBuilder
import net.ccbluex.liquidbounce.features.command.builder.ParameterBuilder
import net.ccbluex.liquidbounce.lang.LanguageManager
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.mc
import net.ccbluex.liquidbounce.utils.client.regular
import net.ccbluex.liquidbounce.utils.client.variable
import net.ccbluex.liquidbounce.web.integration.BrowserScreen
import net.ccbluex.liquidbounce.web.integration.IntegrationHandler
import net.ccbluex.liquidbounce.web.integration.IntegrationHandler.clientJcef
import net.ccbluex.liquidbounce.web.integration.VirtualScreenType
import net.ccbluex.liquidbounce.web.theme.ThemeManager
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent

/**
 * Client Command
 *
 * Provides subcommands for client management.
 */
object CommandClient {

    /**
     * Creates client command with a variety of subcommands.
     *
     * TODO: contributors
     *  links
     *  instructions
     *  reset
     *  theme manager
     */
    fun createCommand() = CommandBuilder.begin("client")
        .hub()
        .subcommand(infoCommand())
        .subcommand(browserCommand())
        .subcommand(integrationCommand())
        .subcommand(languageCommand())
        .build()

    private fun infoCommand() = CommandBuilder
        .begin("info")
        .handler { command, _ ->
            chat(regular(command.result("clientName", variable(LiquidBounce.CLIENT_NAME))),
                prefix = false)
            chat(regular(command.result("clientVersion", variable(LiquidBounce.clientVersion))),
                prefix = false)
            chat(regular(command.result("clientAuthor", variable(LiquidBounce.CLIENT_AUTHOR))),
                prefix = false)
        }.build()

    private fun browserCommand() = CommandBuilder.begin("browser")
        .hub()
        .subcommand(
            CommandBuilder.begin("open")
                .parameter(
                    ParameterBuilder.begin<String>("name")
                        .verifiedBy(ParameterBuilder.STRING_VALIDATOR).required()
                        .build()
                ).handler { command, args ->
                    chat(regular("Opening browser..."))
                    mc.setScreen(BrowserScreen(args[0] as String))
                }.build()
        )
        .build()

    private fun integrationCommand() = CommandBuilder.begin("integration")
        .hub()
        .subcommand(CommandBuilder.begin("menu")
            .alias("url")
            .handler { command, args ->
                chat(variable("Client Integration"))
                chat(
                    regular("URL: ")
                        .append(variable(ThemeManager.getUrl()).styled {
                            it.withUnderline(true)
                                .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, ThemeManager.getUrl()))
                                .withHoverEvent(
                                    HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        regular("Click to open the integration URL in your browser.")
                                    )
                                )
                        }),
                    prefix = false
                )

                chat(prefix = false)
                chat(regular("Integration Menu:"))
                for (menu in VirtualScreenType.entries) {
                    val internalName = menu.internalName

                    val url = runCatching {
                        ThemeManager.getUrl(internalName, true)
                    }.getOrNull() ?: continue
                    val upperFirstName = menu.internalName.replaceFirstChar { it.uppercase() }

                    chat(
                        regular("-> $upperFirstName (")
                            .append(variable("Browser").styled {
                                it.withUnderline(true)
                                    .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, url))
                                    .withHoverEvent(
                                        HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            regular("Click to open the URL in your browser.")
                                        )
                                    )
                            })
                            .append(regular(", "))
                            .append(variable("Clipboard").styled {
                                it.withUnderline(true)
                                    .withClickEvent(ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, url))
                                    .withHoverEvent(
                                        HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            regular("Click to copy the URL to your clipboard.")
                                        )
                                    )
                            })
                            .append(regular(")")),
                        prefix = false
                    )
                }

                chat(variable("Hint: You can also access the integration from another device.")
                    .styled { it.withItalic(true) })
            }.build()
        )
        .subcommand(CommandBuilder.begin("override")
            .parameter(
                ParameterBuilder.begin<String>("name")
                    .verifiedBy(ParameterBuilder.STRING_VALIDATOR).required()
                    .build()
            ).handler { command, args ->
                chat(regular("Overrides client JCEF browser..."))
                clientJcef?.loadUrl(args[0] as String)
            }.build()
        ).subcommand(CommandBuilder.begin("reset")
            .handler { command, args ->
                chat(regular("Resetting client JCEF browser..."))
                IntegrationHandler.updateIntegrationBrowser()
            }.build()
        )
        .build()

    private fun languageCommand() = CommandBuilder.begin("language")
        .hub()
        .subcommand(CommandBuilder.begin("list")
            .handler { command, args ->
                chat(regular("Available languages:"))
                for (language in LanguageManager.knownLanguages) {
                    chat(regular("-> $language"))
                }
            }.build()
        )
        .subcommand(CommandBuilder.begin("set")
            .parameter(
                ParameterBuilder.begin<String>("language")
                    .verifiedBy(ParameterBuilder.STRING_VALIDATOR).required()
                    .build()
            ).handler { command, args ->
                val language = LanguageManager.knownLanguages.find { it.equals(args[0] as String, true) }
                if (language == null) {
                    chat(regular("Language not found."))
                    return@handler
                }

                chat(regular("Setting language to ${language}..."))
                LanguageManager.overrideLanguage = language

                ConfigSystem.storeConfigurable(LanguageManager)
            }.build()
        )
        .subcommand(CommandBuilder.begin("unset")
            .handler { command, args ->
                chat(regular("Unset override language..."))
                LanguageManager.overrideLanguage = ""
                ConfigSystem.storeConfigurable(LanguageManager)
            }.build()
        )
        .build()

}
