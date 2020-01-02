package org.realitix.dfilesearch.filesearch.views;

import io.dropwizard.views.View;

public class InputView extends View {

    private final String string;

    protected InputView(String string) {
        super("string.ftl");
        this.string = string;
    }

    public String getString() {
        return string;
    }

}
