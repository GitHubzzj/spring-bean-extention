package com.byedbl;

import org.junit.Test;

public class PathMatchingResourcePatternResolverTest {


    @Test
    public void testDetermineRootDir() {
        System.out.println(determineRootDir("classpath*:com/byedbl/**/*.class"));
        System.out.println(determineRootDir("classpath*:com/byedbl/**/*.class/"));
        System.out.println(determineRootDir("classpath*:com/byedbl/**/*.clas/s"));
        System.out.println(determineRootDir("classpath*:com/byedbl/**/*.clas/s//"));

    }


    protected String determineRootDir(String location) {
        int prefixEnd = location.indexOf(":") + 1;
        int rootDirEnd = location.length();
        while (rootDirEnd > prefixEnd && isPattern(location.substring(prefixEnd, rootDirEnd))) {
            rootDirEnd = location.lastIndexOf('/', rootDirEnd -1)  ;
//            rootDirEnd = location.lastIndexOf('/', rootDirEnd -2) +1 ;
        }
        if (rootDirEnd == 0) {
            rootDirEnd = prefixEnd;
        }
        rootDirEnd += 1;
        return location.substring(0, rootDirEnd);
    }

    public boolean isPattern(String path) {
        return (path.indexOf('*') != -1 || path.indexOf('?') != -1);
    }
}
