package org.realitix.dfilesearch.filesearch.util;


/*
 * Parses string responses arriving via the UDP port
 * https://docs.oracle.com/javase/tutorial/java/generics/types.html
 */
public interface ResponseParser<T> {

    /**
     * Parsing responses coming to the node
     * @param t response
     */
    void parse(T t);

}
