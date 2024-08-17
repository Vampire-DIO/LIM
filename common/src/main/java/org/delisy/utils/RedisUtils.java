package org.delisy.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author LvWei
 * @Date 2024/7/29 17:04
 */
@Component
public class RedisUtils {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public RedisUtils() {
    }

    public boolean expire(String key, long time) {
        try {
            if (time > 0L) {
                this.redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }

            return true;
        } catch (Exception var5) {
            var5.printStackTrace();
            return false;
        }
    }

    public boolean tryLock(String key,long waitTime, long leaseTime, String uniqueId, int retryCount) {
        try {
            if (retryCount <= 0) {
                return false;
            }
            Boolean lock = this.redisTemplate.opsForValue().setIfAbsent(key, uniqueId, leaseTime, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(lock)) {
                return true;
            }else {
                Thread.sleep(waitTime);
                return tryLock(key,waitTime,leaseTime,uniqueId,retryCount-1);
            }
        } catch (Exception var6) {
            var6.printStackTrace();
            return false;
        }
    }

    public boolean unLock(String key, String uniqueId) {
        try {
            String value = (String)this.redisTemplate.opsForValue().get(key);
            if (value == null || !value.equals(uniqueId)) {
                return false;
            }
            this.redisTemplate.delete(key);
            return true;
        } catch (Exception var3) {
            var3.printStackTrace();
            return false;
        }
    }


    public boolean setNx(String key, String checkKey) {
        try {
            return Boolean.TRUE.equals(this.redisTemplate.opsForValue().setIfAbsent(key, checkKey));
        } catch (Exception var3) {
            var3.printStackTrace();
            return false;
        }
    }

    public Map<String, Object> executeGetPipeline(final String... keys) {
        List<Object> list = this.redisTemplate.executePipelined(new RedisCallback<String>() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                connection.openPipeline();
                String[] var2 = keys;
                int var3 = var2.length;

                for(int var4 = 0; var4 < var3; ++var4) {
                    String key = var2[var4];
                    connection.stringCommands().get(key.getBytes());
                }

                return null;
            }
        }, this.redisTemplate.getStringSerializer());
        Map<String, Object> result = new HashMap();

        for(int i = 0; i < keys.length; ++i) {
            result.put(keys[i], list.get(i));
        }

        return result;
    }

    public Map<String, Object> executeSetPipeline(final Map<String, String> map) {
        List<Object> list = this.redisTemplate.executePipelined(new RedisCallback<String>() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                connection.openPipeline();
                Iterator var2 = map.entrySet().iterator();

                while(var2.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry)var2.next();
                    connection.stringCommands().set(((String)entry.getKey()).getBytes(), ((String)entry.getValue()).getBytes());
                }

                return null;
            }
        });
        Map<String, Object> result = new HashMap();

        for(int i = 0; i < map.size(); ++i) {
            result.put(map.keySet().toArray()[i].toString(), list.get(i));
        }

        return result;
    }

    public Object ifGtSet(String key, String oldValue, String newValue) {
        DefaultRedisScript<Object> script = new DefaultRedisScript();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("GetIfGtSet.lua")));
        script.setResultType(Object.class);
        return this.redisTemplate.execute(script, Collections.singletonList(key), new Object[]{oldValue, newValue});
    }

    public Object ifLtSet(String key, String expectValue, String newValue) {
        DefaultRedisScript<Object> script = new DefaultRedisScript();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("GetIfLtSet.lua")));
        script.setResultType(Object.class);
        return this.redisTemplate.execute(script, Collections.singletonList(key), new Object[]{expectValue, newValue});
    }

    public Object ifEqualSet(String key, String expectValue, String newValue) {
        DefaultRedisScript<Object> script = new DefaultRedisScript();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("GetIfEqualSet.lua")));
        script.setResultType(Object.class);
        return this.redisTemplate.execute(script, Collections.singletonList(key), new Object[]{expectValue, newValue});
    }

    public <T> T luaScript(DefaultRedisScript<T> redisScript, List<String> keys, Object... args) {
        return this.redisTemplate.execute(redisScript, keys, args);
    }

    public <T> T luaScript(Class<T> clazz, String script, List<String> keys, Object... args) {
        DefaultRedisScript<T> redisScript = new DefaultRedisScript();
        redisScript.setResultType(clazz);
        redisScript.setScriptText(script);
        return this.luaScript(redisScript, keys, args);
    }

    public long getExpire(String key) {
        return this.redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public boolean hasKey(String key) {
        try {
            return this.redisTemplate.hasKey(key);
        } catch (Exception var3) {
            var3.printStackTrace();
            return false;
        }
    }

    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                this.redisTemplate.delete(key[0]);
            } else {
                this.redisTemplate.delete(Arrays.asList(key));
            }
        }

    }

    public Object getAndDelete(String key) {
        Object var2;
        try {
            var2 = key == null ? null : this.get(key);
        } finally {
            this.del(key);
        }

        return var2;
    }

    public Object get(String key) {
        return key == null ? null : this.redisTemplate.opsForValue().get(key);
    }

    public boolean set(String key, String value) {
        try {
            this.redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception var4) {
            var4.printStackTrace();
            return false;
        }
    }

    public boolean set(String key, String value, long time) {
        try {
            if (time > 0L) {
                this.redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            }

            return true;
        } catch (Exception var6) {
            var6.printStackTrace();
            return false;
        }
    }

    public long incr(String key, long delta) {
        if (delta < 0L) {
            throw new RuntimeException("递增因子必须大于0");
        } else {
            return this.redisTemplate.opsForValue().increment(key, delta);
        }
    }

    public long decr(String key, long delta) {
        if (delta < 0L) {
            throw new RuntimeException("递减因子必须大于0");
        } else {
            return this.redisTemplate.opsForValue().increment(key, -delta);
        }
    }

    public Object hget(String key, String item) {
        return this.redisTemplate.opsForHash().get(key, item);
    }

    public Map<Object, Object> hmget(String key) {
        return this.redisTemplate.opsForHash().entries(key);
    }

    public boolean hmset(String key, Map<String, Object> map) {
        try {
            this.redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception var4) {
            var4.printStackTrace();
            return false;
        }
    }

    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            if (time > 0L) {
                this.redisTemplate.opsForHash().putAll(key, map);
                this.expire(key, time);
            }

            return true;
        } catch (Exception var6) {
            var6.printStackTrace();
            return false;
        }
    }

    public boolean hset(String key, String item, Object value) {
        try {
            this.redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception var5) {
            var5.printStackTrace();
            return false;
        }
    }

    public boolean hset(String key, String item, Object value, long time) {
        try {
            if (time > 0L) {
                this.redisTemplate.opsForHash().put(key, item, value);
                this.expire(key, time);
            }

            return true;
        } catch (Exception var7) {
            var7.printStackTrace();
            return false;
        }
    }

    public void hdel(String key, Object... item) {
        this.redisTemplate.opsForHash().delete(key, item);
    }

    public boolean hHasKey(String key, String item) {
        return this.redisTemplate.opsForHash().hasKey(key, item);
    }

    public double hincr(String key, String item, double by) {
        return this.redisTemplate.opsForHash().increment(key, item, by);
    }

    public double hdecr(String key, String item, double by) {
        return this.redisTemplate.opsForHash().increment(key, item, -by);
    }

    public Set<Object> sGet(String key) {
        try {
            return this.redisTemplate.opsForSet().members(key);
        } catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public boolean sHasKey(String key, Object value) {
        try {
            return this.redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception var4) {
            var4.printStackTrace();
            return false;
        }
    }

    public long sSet(String key, Object... values) {
        try {
            return this.redisTemplate.opsForSet().add(key, values);
        } catch (Exception var4) {
            var4.printStackTrace();
            return 0L;
        }
    }

    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = (long)values.length;
            if (time > 0L) {
                count = this.redisTemplate.opsForSet().add(key, values);
                this.expire(key, time);
            }

            return count;
        } catch (Exception var6) {
            var6.printStackTrace();
            return 0L;
        }
    }

    public long sGetSetSize(String key) {
        try {
            return this.redisTemplate.opsForSet().size(key);
        } catch (Exception var3) {
            var3.printStackTrace();
            return 0L;
        }
    }

    public long setRemove(String key, Object... values) {
        try {
            return this.redisTemplate.opsForSet().remove(key, values);
        } catch (Exception var4) {
            var4.printStackTrace();
            return 0L;
        }
    }

    public long sIntersectAndStore(String key, String otherKey, String storeKey) {
        Long size = this.redisTemplate.opsForSet().intersectAndStore(key, otherKey, storeKey);
        if (size == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return size;
        }
    }

    public Set<Object> sUnion(String key, String otherKey) {
        Set<Object> set = this.redisTemplate.opsForSet().union(key, otherKey);
        if (set == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return set;
        }
    }

    public long sUnionAndStore(String key, String otherKey, String storeKey) {
        Long size = this.redisTemplate.opsForSet().unionAndStore(key, otherKey, storeKey);
        if (size == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return size;
        }
    }

    public Set<Object> sDifference(String key, String otherKey) {
        Set<Object> set = this.redisTemplate.opsForSet().difference(key, otherKey);
        if (set == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return set;
        }
    }

    public long sDifferenceAndStore(String key, String otherKey, String storeKey) {
        Long size = this.redisTemplate.opsForSet().differenceAndStore(key, otherKey, storeKey);
        if (size == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return size;
        }
    }

    public Object sRandomMember(String key) {
        return this.redisTemplate.opsForSet().randomMember(key);
    }

    public List<Object> sRandomMembers(String key, int count) {
        return this.redisTemplate.opsForSet().randomMembers(key, (long)count);
    }

    public Set<Object> sDistinctRandomMembers(String key, int count) {
        return this.redisTemplate.opsForSet().distinctRandomMembers(key, (long)count);
    }

    public boolean zAdd(String key, String item, double score) {
        return Boolean.TRUE.equals(this.redisTemplate.opsForZSet().add(key, item, score));
    }

    public long zAdd(String key, Set<ZSetOperations.TypedTuple<Object>> entries) {
        Long count = this.redisTemplate.opsForZSet().add(key, entries);
        if (count == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return count;
        }
    }

    public long zRemove(String key, Object... items) {
        Long count = this.redisTemplate.opsForZSet().remove(key, items);
        if (count == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return count;
        }
    }

    public long zRemoveRange(String key, long start, long end) {
        Long count = this.redisTemplate.opsForZSet().removeRange(key, start, end);
        if (count == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return count;
        }
    }

    public long zRemoveRangeByScore(String key, double minScore, double maxScore) {
        Long count = this.redisTemplate.opsForZSet().removeRangeByScore(key, minScore, maxScore);
        if (count == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return count;
        }
    }

    public double zIncrementScore(String key, Object item, double by) {
        Double score = this.redisTemplate.opsForZSet().incrementScore(key, item, by);
        if (score == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return score;
        }
    }

    public long zRank(String key, Object item) {
        Long rank = this.redisTemplate.opsForZSet().rank(key, item);
        if (rank == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return rank;
        }
    }

    public long zReverseRank(String key, Object item) {
        Long rank = this.redisTemplate.opsForZSet().reverseRank(key, item);
        if (rank == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return rank;
        }
    }

    public Set<Object> zRange(String key, long start, long end) {
        return this.redisTemplate.opsForZSet().range(key, start, end);
    }

    public Set<Object> zWholeZSetItem(String key) {
        return this.redisTemplate.opsForZSet().range(key, 0L, -1L);
    }

    public Set<ZSetOperations.TypedTuple<Object>> zRangeWithScores(String key, long start, long end) {
        return this.redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    public Set<ZSetOperations.TypedTuple<Object>> zWholeZSetEntry(String key) {
        Set<ZSetOperations.TypedTuple<Object>> entries = this.redisTemplate.opsForZSet().rangeWithScores(key, 0L, -1L);
        return entries;
    }

    public Set<Object> zRangeByScore(String key, double minScore, double maxScore) {
        Set<Object> items = this.redisTemplate.opsForZSet().rangeByScore(key, minScore, maxScore);
        return items;
    }

    public Set<Object> zRangeByScore(String key, double minScore, double maxScore, long offset, long count) {
        Set<Object> items = this.redisTemplate.opsForZSet().rangeByScore(key, minScore, maxScore, offset, count);
        return items;
    }

    public Set<ZSetOperations.TypedTuple<Object>> zRangeByScoreWithScores(String key, double minScore, double maxScore) {
        Set<ZSetOperations.TypedTuple<Object>> entries = this.redisTemplate.opsForZSet().rangeByScoreWithScores(key, minScore, maxScore);
        return entries;
    }

    public Set<ZSetOperations.TypedTuple<Object>> zRangeByScoreWithScores(String key, double minScore, double maxScore, long offset, long count) {
        Set<ZSetOperations.TypedTuple<Object>> entries = this.redisTemplate.opsForZSet().rangeByScoreWithScores(key, minScore, maxScore, offset, count);
        return entries;
    }

    public Set<Object> zReverseRange(String key, long start, long end) {
        Set<Object> entries = this.redisTemplate.opsForZSet().reverseRange(key, start, end);
        return entries;
    }

    public Set<ZSetOperations.TypedTuple<Object>> zReverseRangeWithScores(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<Object>> entries = this.redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        return entries;
    }

    public Set<Object> zReverseRangeByScore(String key, double minScore, double maxScore) {
        Set<Object> items = this.redisTemplate.opsForZSet().reverseRangeByScore(key, minScore, maxScore);
        return items;
    }

    public Set<ZSetOperations.TypedTuple<Object>> zReverseRangeByScoreWithScores(String key, double minScore, double maxScore) {
        Set<ZSetOperations.TypedTuple<Object>> entries = this.redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, minScore, maxScore);
        return entries;
    }

    public Set<Object> zReverseRangeByScore(String key, double minScore, double maxScore, long offset, long count) {
        Set<Object> items = this.redisTemplate.opsForZSet().reverseRangeByScore(key, minScore, maxScore, offset, count);
        return items;
    }

    public long zCount(String key, double minScore, double maxScore) {
        Long count = this.redisTemplate.opsForZSet().count(key, minScore, maxScore);
        if (count == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return count;
        }
    }

    public long zSize(String key) {
        Long size = this.redisTemplate.opsForZSet().size(key);
        if (size == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return size;
        }
    }

    public long zZCard(String key) {
        Long size = this.redisTemplate.opsForZSet().zCard(key);
        if (size == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return size;
        }
    }

    public double zScore(String key, Object item) {
        Double score = this.redisTemplate.opsForZSet().score(key, item);
        if (score == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return score;
        }
    }

    public long zUnionAndStore(String key, String otherKey, String storeKey) {
        Long size = this.redisTemplate.opsForZSet().unionAndStore(key, otherKey, storeKey);
        if (size == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return size;
        }
    }

    public long zUnionAndStore(String key, Collection<String> otherKeys, String storeKey) {
        Long size = this.redisTemplate.opsForZSet().unionAndStore(key, otherKeys, storeKey);
        if (size == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return size;
        }
    }

    public long zIntersectAndStore(String key, String otherKey, String storeKey) {
        Long size = this.redisTemplate.opsForZSet().intersectAndStore(key, otherKey, storeKey);
        if (size == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return size;
        }
    }

    public long zIntersectAndStore(String key, Collection<String> otherKeys, String storeKey) {
        Long size = this.redisTemplate.opsForZSet().intersectAndStore(key, otherKeys, storeKey);
        if (size == null) {
            throw new RuntimeException("redis result is null");
        } else {
            return size;
        }
    }

    public List<Object> lGet(String key, long start, long end) {
        try {
            return this.redisTemplate.opsForList().range(key, start, end);
        } catch (Exception var7) {
            var7.printStackTrace();
            return null;
        }
    }

    public long lGetListSize(String key) {
        try {
            return this.redisTemplate.opsForList().size(key);
        } catch (Exception var3) {
            var3.printStackTrace();
            return 0L;
        }
    }

    public Object lGetIndex(String key, long index) {
        try {
            return this.redisTemplate.opsForList().index(key, index);
        } catch (Exception var5) {
            var5.printStackTrace();
            return null;
        }
    }

    public boolean lRightPush(String key, Object value) {
        try {
            this.redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception var4) {
            var4.printStackTrace();
            return false;
        }
    }

    public boolean lLeftPush(String key, Object value) {
        try {
            this.redisTemplate.opsForList().leftPush(key, value);
            return true;
        } catch (Exception var4) {
            var4.printStackTrace();
            return false;
        }
    }

    public boolean lRightPush(String key, Object value, long time) {
        try {
            if (time > 0L) {
                this.redisTemplate.opsForList().rightPush(key, value);
                this.expire(key, time);
            }

            return true;
        } catch (Exception var6) {
            var6.printStackTrace();
            return false;
        }
    }

    public boolean lLeftPush(String key, Object value, long time) {
        try {
            if (time > 0L) {
                this.redisTemplate.opsForList().leftPush(key, value);
                this.expire(key, time);
            }

            return true;
        } catch (Exception var6) {
            var6.printStackTrace();
            return false;
        }
    }

    public boolean lRightPush(String key, List<Object> value) {
        try {
            this.redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception var4) {
            var4.printStackTrace();
            return false;
        }
    }

    public boolean lLeftPush(String key, List<Object> value) {
        try {
            this.redisTemplate.opsForList().leftPushAll(key, value);
            return true;
        } catch (Exception var4) {
            var4.printStackTrace();
            return false;
        }
    }

    public boolean lRightPush(String key, List<Object> value, long time) {
        try {
            if (time > 0L) {
                this.redisTemplate.opsForList().rightPushAll(key, value);
                this.expire(key, time);
            }

            return true;
        } catch (Exception var6) {
            var6.printStackTrace();
            return false;
        }
    }

    public boolean lLeftPush(String key, List<Object> value, long time) {
        try {
            if (time > 0L) {
                this.redisTemplate.opsForList().leftPushAll(key, value);
                this.expire(key, time);
            }

            return true;
        } catch (Exception var6) {
            var6.printStackTrace();
            return false;
        }
    }

    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            this.redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception var6) {
            var6.printStackTrace();
            return false;
        }
    }

    public long lRemove(String key, long count, Object value) {
        try {
            return this.redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception var6) {
            var6.printStackTrace();
            return 0L;
        }
    }
}
