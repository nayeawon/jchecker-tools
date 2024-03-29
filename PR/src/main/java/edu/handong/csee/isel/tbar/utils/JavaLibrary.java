package edu.handong.csee.isel.tbar.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;

public class JavaLibrary {
    public URL[] classPathFrom(String classpath) {
        List<String> folderNames = split(classpath, classpathSeparator());
        URL[] folders = new URL[folderNames.size()];
        int index = 0;
        for (String folderName : folderNames) {
            folders[index] = urlFrom(folderName);
            index += 1;
        }
        return folders;
    }

    public URL[] extendClassPathWith(String classpath, URL[] destination) {
        List<URL> extended = newLinkedList(destination);
        extended.addAll(asList(classPathFrom(classpath)));
        return extended.toArray(new URL[extended.size()]);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> newLinkedList(T... elements) {
        return newLinkedList(asList(elements));
    }

    private <T> List<T> newLinkedList(Collection<? extends T> collection) {
        List<T> newList = newLinkedList();
        return (List<T>) withAll(newList, collection);
    }

    private <T> List<T> newLinkedList() {
        return new LinkedList<T>();
    }

    private <T> Collection<T> withAll(Collection<T> destination, Iterable<? extends T> elements) {
        addAll(destination, elements);
        return destination;
    }

    private <T> boolean addAll(Collection<T> destination, Iterable<? extends T> elements) {
        boolean changed = false;
        for (T element : elements) {
            changed |= destination.add(element);
        }
        return changed;
    }

    private List<String> split(String chainedStrings, Character character) {
        return split(chainedStrings, format("[%c]", character));
    }

    private List<String> split(String chainedStrings, String splittingRegex) {
        return asList(chainedStrings.split(splittingRegex));
    }

    private URL urlFrom(String path) {
        URL url = null;
        try {
            url = openFrom(path).toURI().toURL();
        } catch (MalformedURLException e) {
            fail("Illegal name for '" + path + "' while converting to URL");
        }
        return url;
    }

    private File openFrom(String path) {
        File file = new File(path);
        if (!file.exists()) {
            fail("File does not exist in: '" + path + "'");
        }
        return file;
    }

    private void fail(String message) {
        throw new IllegalArgumentException(message);
    }

    private Character classpathSeparator() {
        if (javaPathSeparator == null) {
            javaPathSeparator = File.pathSeparatorChar;
        }
        return javaPathSeparator;
    }

    private Character javaPathSeparator;
}
