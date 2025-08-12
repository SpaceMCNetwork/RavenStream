package me.daniramos.ravenstream.legacy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public final class LegacyProcessor implements UnaryOperator<Component> {
    private static final Replacer REPLACER = new Replacer();

    public Component apply(Component component) {
        return component.replaceText(REPLACER);
    }

    private static final class Replacer implements Consumer<TextReplacementConfig.Builder> {
        private static final Pattern ALL = Pattern.compile(".*");

        private static final Replacement REPLACEMENT = new Replacement();

        public void accept(TextReplacementConfig.Builder builder) {
            builder
                    .match(ALL)
                    .replacement(REPLACEMENT);
        }
    }

    private static final class Replacement implements BiFunction<MatchResult, TextComponent.Builder, ComponentLike> {
        public ComponentLike apply(MatchResult matchResult, TextComponent.Builder builder) {
            return Legacy.LEGACY_AMPERSAND_SERIALIZER.deserialize(matchResult.group());
        }
    }
}