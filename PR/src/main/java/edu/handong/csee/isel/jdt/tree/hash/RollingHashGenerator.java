package edu.handong.csee.isel.jdt.tree.hash;

import edu.handong.csee.isel.jdt.tree.ITree;

import java.util.HashMap;
import java.util.Map;

import static edu.handong.csee.isel.jdt.tree.hash.HashUtils.*;

public abstract class RollingHashGenerator implements HashGenerator {
    public void hash(ITree t) {
        for (ITree n: t.postOrder())
            if (n.isLeaf())
                n.setHash(leafHash(n));
            else
                n.setHash(innerNodeHash(n));
    }

    public abstract int hashFunction(String s);

    public int leafHash(ITree t) {
        return BASE * hashFunction(HashUtils.inSeed(t)) + hashFunction(HashUtils.outSeed(t));
    }

    public int innerNodeHash(ITree t) {
        int size = t.getSize() * 2 - 1;
        int hash = hashFunction(HashUtils.inSeed(t)) * fpow(BASE, size);

        for (ITree c: t.getChildren()) {
            size = size - c.getSize() * 2;
            hash += c.getHash() * fpow(BASE, size);
        }

        hash += hashFunction(HashUtils.outSeed(t));
        return hash;
    }

    public class JavaRollingHashGenerator extends RollingHashGenerator {

        @Override
        public int hashFunction(String s) {
            return s.hashCode();
        }

    }

    public static class Md5RollingHashGenerator extends RollingHashGenerator {

        @Override
        public int hashFunction(String s) {
            return md5(s);
        }

    }

    public class RandomRollingHashGenerator extends RollingHashGenerator {

        private Map<String, Integer> digests = new HashMap<>();

        @Override
        public int hashFunction(String s) {
            return rdmHash(s);
        }

        public int rdmHash(String s) {
            if (!digests.containsKey(s)) {
                int digest = (int) (Math.random() * (Integer.MAX_VALUE - 1));
                digests.put(s, digest);
                return digest;
            } else return digests.get(s);
        }

    }
}
