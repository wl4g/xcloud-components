/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.component.support.cache.jedis.cluster;

import static org.springframework.util.Assert.notNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wl4g.component.support.cache.jedis.JedisClient;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.BitOP;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ListPosition;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.params.ZAddParams;
import redis.clients.jedis.params.ZIncrByParams;

/**
 * {@link JedisCluster} jedis client wrapper.
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-08-03
 * @since
 */
public class JedisClusterJedisClient implements JedisClient {

    /** {@link JedisCluster} */
    protected final JedisCluster jedisCluster;

    public JedisClusterJedisClient(JedisCluster jedisCluster) {
        notNull(jedisCluster, "jedisCluster");
        this.jedisCluster = jedisCluster;
    }

    @Override
    public Map<String, JedisPool> getClusterNodes() {
        return jedisCluster.getClusterNodes();
    }

    @Override
    public void close() throws IOException {
        jedisCluster.close();
    }

    public Jedis getConnectionFromSlot(int slot) {
        return jedisCluster.getConnectionFromSlot(slot);
    }

    @Override
    public String set(final byte[] key, final byte[] value) {
        return jedisCluster.set(key, value);
    }

    @Override
    public String set(final byte[] key, final byte[] value, final SetParams params) {
        return jedisCluster.set(key, value, params);
    }

    @Override
    public byte[] get(final byte[] key) {
        return jedisCluster.get(key);
    }

    @Override
    public Long exists(final byte[]... keys) {
        return jedisCluster.exists(keys);
    }

    @Override
    public Boolean exists(final byte[] key) {
        return jedisCluster.exists(key);
    }

    @Override
    public Long persist(final byte[] key) {
        return jedisCluster.persist(key);
    }

    @Override
    public String type(final byte[] key) {
        return jedisCluster.type(key);
    }

    @Override
    public byte[] dump(final byte[] key) {
        return jedisCluster.dump(key);
    }

    @Override
    public String restore(final byte[] key, final int ttl, final byte[] serializedValue) {
        return jedisCluster.restore(key, ttl, serializedValue);
    }

    @Override
    public Long expire(final byte[] key, final int seconds) {
        return jedisCluster.expire(key, seconds);
    }

    @Override
    public Long pexpire(final byte[] key, final long milliseconds) {
        return jedisCluster.pexpire(key, milliseconds);
    }

    @Override
    public Long expireAt(final byte[] key, final long unixTime) {
        return jedisCluster.expireAt(key, unixTime);
    }

    @Override
    public Long pexpireAt(final byte[] key, final long millisecondsTimestamp) {
        return jedisCluster.pexpireAt(key, millisecondsTimestamp);
    }

    @Override
    public Long ttl(final byte[] key) {
        return jedisCluster.ttl(key);
    }

    @Override
    public Long pttl(final byte[] key) {
        return jedisCluster.pttl(key);
    }

    @Override
    public Long touch(final byte[] key) {
        return jedisCluster.touch(key);
    }

    @Override
    public Long touch(final byte[]... keys) {
        return jedisCluster.touch(keys);
    }

    @Override
    public Boolean setbit(final byte[] key, final long offset, final boolean value) {
        return jedisCluster.setbit(key, offset, value);
    }

    @Override
    public Boolean setbit(final byte[] key, final long offset, final byte[] value) {
        return jedisCluster.setbit(key, offset, value);
    }

    @Override
    public Boolean getbit(final byte[] key, final long offset) {
        return jedisCluster.getbit(key, offset);
    }

    @Override
    public Long setrange(final byte[] key, final long offset, final byte[] value) {
        return jedisCluster.setrange(key, offset, value);
    }

    @Override
    public byte[] getrange(final byte[] key, final long startOffset, final long endOffset) {
        return jedisCluster.getrange(key, startOffset, endOffset);
    }

    @Override
    public byte[] getSet(final byte[] key, final byte[] value) {
        return jedisCluster.getSet(key, value);
    }

    @Override
    public Long setnx(final byte[] key, final byte[] value) {
        return jedisCluster.setnx(key, value);
    }

    @Override
    public String psetex(final byte[] key, final long milliseconds, final byte[] value) {
        return jedisCluster.psetex(key, milliseconds, value);
    }

    @Override
    public String setex(final byte[] key, final int seconds, final byte[] value) {
        return jedisCluster.setex(key, seconds, value);
    }

    @Override
    public Long decrBy(final byte[] key, final long decrement) {
        return jedisCluster.decrBy(key, decrement);
    }

    @Override
    public Long decr(final byte[] key) {
        return jedisCluster.decr(key);
    }

    @Override
    public Long incrBy(final byte[] key, final long increment) {
        return jedisCluster.incrBy(key, increment);
    }

    @Override
    public Double incrByFloat(final byte[] key, final double increment) {
        return jedisCluster.incrByFloat(key, increment);
    }

    @Override
    public Long incr(final byte[] key) {
        return jedisCluster.incr(key);
    }

    @Override
    public Long append(final byte[] key, final byte[] value) {
        return jedisCluster.append(key, value);
    }

    @Override
    public byte[] substr(final byte[] key, final int start, final int end) {
        return jedisCluster.substr(key, start, end);
    }

    @Override
    public Long hset(final byte[] key, final byte[] field, final byte[] value) {
        return jedisCluster.hset(key, field, value);
    }

    @Override
    public Long hset(final byte[] key, final Map<byte[], byte[]> hash) {
        return jedisCluster.hset(key, hash);
    }

    @Override
    public byte[] hget(final byte[] key, final byte[] field) {
        return jedisCluster.hget(key, field);
    }

    @Override
    public Long hsetnx(final byte[] key, final byte[] field, final byte[] value) {
        return jedisCluster.hsetnx(key, field, value);
    }

    @Override
    public String hmset(final byte[] key, final Map<byte[], byte[]> hash) {
        return jedisCluster.hmset(key, hash);
    }

    @Override
    public List<byte[]> hmget(final byte[] key, final byte[]... fields) {
        return jedisCluster.hmget(key, fields);
    }

    @Override
    public Long hincrBy(final byte[] key, final byte[] field, final long value) {
        return jedisCluster.hincrBy(key, field, value);
    }

    @Override
    public Double hincrByFloat(final byte[] key, final byte[] field, final double value) {
        return jedisCluster.hincrByFloat(key, field, value);
    }

    @Override
    public Boolean hexists(final byte[] key, final byte[] field) {
        return jedisCluster.hexists(key, field);
    }

    @Override
    public Long hdel(final byte[] key, final byte[]... field) {
        return jedisCluster.hdel(key, field);
    }

    @Override
    public Long hlen(final byte[] key) {
        return jedisCluster.hlen(key);
    }

    @Override
    public Set<byte[]> hkeys(final byte[] key) {
        return jedisCluster.hkeys(key);
    }

    @Override
    public Collection<byte[]> hvals(final byte[] key) {
        return jedisCluster.hvals(key);
    }

    @Override
    public Map<byte[], byte[]> hgetAll(final byte[] key) {
        return jedisCluster.hgetAll(key);
    }

    @Override
    public Long rpush(final byte[] key, final byte[]... args) {
        return jedisCluster.rpush(key, args);
    }

    @Override
    public Long lpush(final byte[] key, final byte[]... args) {
        return jedisCluster.lpush(key, args);
    }

    @Override
    public Long llen(final byte[] key) {
        return jedisCluster.llen(key);
    }

    @Override
    public List<byte[]> lrange(final byte[] key, final long start, final long stop) {
        return jedisCluster.lrange(key, start, stop);
    }

    @Override
    public String ltrim(final byte[] key, final long start, final long stop) {
        return jedisCluster.ltrim(key, start, stop);
    }

    @Override
    public byte[] lindex(final byte[] key, final long index) {
        return jedisCluster.lindex(key, index);
    }

    @Override
    public String lset(final byte[] key, final long index, final byte[] value) {
        return jedisCluster.lset(key, index, value);
    }

    @Override
    public Long lrem(final byte[] key, final long count, final byte[] value) {
        return jedisCluster.lrem(key, count, value);
    }

    @Override
    public byte[] lpop(final byte[] key) {
        return jedisCluster.lpop(key);
    }

    @Override
    public byte[] rpop(final byte[] key) {
        return jedisCluster.rpop(key);
    }

    @Override
    public Long sadd(final byte[] key, final byte[]... member) {
        return jedisCluster.sadd(key, member);
    }

    @Override
    public Set<byte[]> smembers(final byte[] key) {
        return jedisCluster.smembers(key);
    }

    @Override
    public Long srem(final byte[] key, final byte[]... member) {
        return jedisCluster.srem(key, member);
    }

    @Override
    public byte[] spop(final byte[] key) {
        return jedisCluster.spop(key);
    }

    @Override
    public Set<byte[]> spop(final byte[] key, final long count) {
        return jedisCluster.spop(key, count);
    }

    @Override
    public Long scard(final byte[] key) {
        return jedisCluster.scard(key);
    }

    @Override
    public Boolean sismember(final byte[] key, final byte[] member) {
        return jedisCluster.sismember(key, member);
    }

    @Override
    public byte[] srandmember(final byte[] key) {
        return jedisCluster.srandmember(key);
    }

    @Override
    public Long strlen(final byte[] key) {
        return jedisCluster.strlen(key);
    }

    @Override
    public Long zadd(final byte[] key, final double score, final byte[] member) {
        return jedisCluster.zadd(key, score, member);
    }

    @Override
    public Long zadd(final byte[] key, final double score, final byte[] member, final ZAddParams params) {
        return jedisCluster.zadd(key, score, member, params);
    }

    @Override
    public Long zadd(final byte[] key, final Map<byte[], Double> scoreMembers) {
        return jedisCluster.zadd(key, scoreMembers);
    }

    @Override
    public Long zadd(final byte[] key, final Map<byte[], Double> scoreMembers, final ZAddParams params) {
        return jedisCluster.zadd(key, scoreMembers, params);
    }

    @Override
    public Set<byte[]> zrange(final byte[] key, final long start, final long stop) {
        return jedisCluster.zrange(key, start, stop);
    }

    @Override
    public Long zrem(final byte[] key, final byte[]... members) {
        return jedisCluster.zrem(key, members);
    }

    @Override
    public Double zincrby(final byte[] key, final double increment, final byte[] member) {
        return jedisCluster.zincrby(key, increment, member);
    }

    @Override
    public Double zincrby(final byte[] key, final double increment, final byte[] member, final ZIncrByParams params) {
        return jedisCluster.zincrby(key, increment, member, params);
    }

    @Override
    public Long zrank(final byte[] key, final byte[] member) {
        return jedisCluster.zrank(key, member);
    }

    @Override
    public Long zrevrank(final byte[] key, final byte[] member) {
        return jedisCluster.zrevrank(key, member);
    }

    @Override
    public Set<byte[]> zrevrange(final byte[] key, final long start, final long stop) {
        return jedisCluster.zrevrange(key, start, stop);
    }

    @Override
    public Set<Tuple> zrangeWithScores(final byte[] key, final long start, final long stop) {
        return jedisCluster.zrangeWithScores(key, start, stop);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(final byte[] key, final long start, final long stop) {
        return jedisCluster.zrevrangeWithScores(key, start, stop);
    }

    @Override
    public Long zcard(final byte[] key) {
        return jedisCluster.zcard(key);
    }

    @Override
    public Double zscore(final byte[] key, final byte[] member) {
        return jedisCluster.zscore(key, member);
    }

    @Override
    public List<byte[]> sort(final byte[] key) {
        return jedisCluster.sort(key);
    }

    @Override
    public List<byte[]> sort(final byte[] key, final SortingParams sortingParameters) {
        return jedisCluster.sort(key, sortingParameters);
    }

    @Override
    public Long zcount(final byte[] key, final double min, final double max) {
        return jedisCluster.zcount(key, min, max);
    }

    @Override
    public Long zcount(final byte[] key, final byte[] min, final byte[] max) {
        return jedisCluster.zcount(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByScore(final byte[] key, final double min, final double max) {
        return jedisCluster.zrangeByScore(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByScore(final byte[] key, final byte[] min, final byte[] max) {
        return jedisCluster.zrangeByScore(key, min, max);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(final byte[] key, final double max, final double min) {
        return jedisCluster.zrevrangeByScore(key, max, min);
    }

    @Override
    public Set<byte[]> zrangeByScore(final byte[] key, final double min, final double max, final int offset, final int count) {
        return jedisCluster.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(final byte[] key, final byte[] max, final byte[] min) {
        return jedisCluster.zrevrangeByScore(key, max, min);
    }

    @Override
    public Set<byte[]> zrangeByScore(final byte[] key, final byte[] min, final byte[] max, final int offset, final int count) {
        return jedisCluster.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(final byte[] key, final double max, final double min, final int offset, final int count) {
        return jedisCluster.zrevrangeByScore(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final double min, final double max) {
        return jedisCluster.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final double max, final double min) {
        return jedisCluster.zrevrangeByScoreWithScores(key, max, min);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final double min, final double max, final int offset,
            final int count) {
        return jedisCluster.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(final byte[] key, final byte[] max, final byte[] min, final int offset, final int count) {
        return jedisCluster.zrevrangeByScore(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final byte[] min, final byte[] max) {
        return jedisCluster.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final byte[] max, final byte[] min) {
        return jedisCluster.zrevrangeByScoreWithScores(key, max, min);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final byte[] min, final byte[] max, final int offset,
            final int count) {
        return jedisCluster.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final double max, final double min, final int offset,
            final int count) {
        return jedisCluster.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final byte[] max, final byte[] min, final int offset,
            final int count) {
        return jedisCluster.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    @Override
    public Long zremrangeByRank(final byte[] key, final long start, final long stop) {
        return jedisCluster.zremrangeByRank(key, start, stop);
    }

    @Override
    public Long zremrangeByScore(final byte[] key, final double min, final double max) {
        return jedisCluster.zremrangeByScore(key, min, max);
    }

    @Override
    public Long zremrangeByScore(final byte[] key, final byte[] min, final byte[] max) {
        return jedisCluster.zremrangeByScore(key, min, max);
    }

    @Override
    public Long linsert(final byte[] key, final ListPosition where, final byte[] pivot, final byte[] value) {
        return jedisCluster.linsert(key, where, pivot, value);
    }

    @Override
    public Long lpushx(final byte[] key, final byte[]... arg) {
        return jedisCluster.lpushx(key, arg);
    }

    @Override
    public Long rpushx(final byte[] key, final byte[]... arg) {
        return jedisCluster.rpushx(key, arg);
    }

    @Override
    public Long del(final byte[] key) {
        return jedisCluster.del(key);
    }

    @Override
    public Long unlink(final byte[] key) {
        return jedisCluster.unlink(key);
    }

    @Override
    public Long unlink(final byte[]... keys) {
        return jedisCluster.unlink(keys);
    }

    @Override
    public byte[] echo(final byte[] arg) {
        return jedisCluster.echo(arg);
    }

    @Override
    public Long bitcount(final byte[] key) {
        return jedisCluster.bitcount(key);
    }

    @Override
    public Long bitcount(final byte[] key, final long start, final long end) {
        return jedisCluster.bitcount(key, start, end);
    }

    @Override
    public Long pfadd(final byte[] key, final byte[]... elements) {
        return jedisCluster.pfadd(key, elements);
    }

    @Override
    public long pfcount(final byte[] key) {
        return jedisCluster.pfcount(key);
    }

    @Override
    public List<byte[]> srandmember(final byte[] key, final int count) {
        return jedisCluster.srandmember(key, count);
    }

    @Override
    public Long zlexcount(final byte[] key, final byte[] min, final byte[] max) {
        return jedisCluster.zlexcount(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByLex(final byte[] key, final byte[] min, final byte[] max) {
        return jedisCluster.zrangeByLex(key, min, max);
    }

    @Override
    public Set<byte[]> zrangeByLex(final byte[] key, final byte[] min, final byte[] max, final int offset, final int count) {
        return jedisCluster.zrangeByLex(key, min, max, offset, count);
    }

    @Override
    public Set<byte[]> zrevrangeByLex(final byte[] key, final byte[] max, final byte[] min) {
        return jedisCluster.zrevrangeByLex(key, max, min);
    }

    @Override
    public Set<byte[]> zrevrangeByLex(final byte[] key, final byte[] max, final byte[] min, final int offset, final int count) {
        return jedisCluster.zrevrangeByLex(key, max, min, offset, count);
    }

    @Override
    public Long zremrangeByLex(final byte[] key, final byte[] min, final byte[] max) {
        return jedisCluster.zremrangeByLex(key, min, max);
    }

    @Override
    public Object eval(final byte[] script, final byte[] keyCount, final byte[]... params) {
        return jedisCluster.eval(script, keyCount, params);
    }

    @Override
    public Object eval(final byte[] script, final int keyCount, final byte[]... params) {
        return jedisCluster.eval(script, keyCount, params);
    }

    @Override
    public Object eval(final byte[] script, final List<byte[]> keys, final List<byte[]> args) {
        return jedisCluster.eval(script, keys, args);
    }

    @Override
    public Object eval(final byte[] script, final byte[] sampleKey) {
        return jedisCluster.eval(script, sampleKey);
    }

    @Override
    public Object evalsha(final byte[] sha1, final byte[] sampleKey) {
        return jedisCluster.evalsha(sha1, sampleKey);
    }

    @Override
    public Object evalsha(final byte[] sha1, final List<byte[]> keys, final List<byte[]> args) {
        return jedisCluster.evalsha(sha1, keys, args);
    }

    @Override
    public Object evalsha(final byte[] sha1, final int keyCount, final byte[]... params) {
        return jedisCluster.evalsha(sha1, keyCount, params);
    }

    @Override
    public List<Long> scriptExists(final byte[] sampleKey, final byte[]... sha1) {
        return jedisCluster.scriptExists(sampleKey, sha1);
    }

    @Override
    public byte[] scriptLoad(final byte[] script, final byte[] sampleKey) {
        return jedisCluster.scriptLoad(script, sampleKey);
    }

    @Override
    public String scriptFlush(final byte[] sampleKey) {
        return jedisCluster.scriptFlush(sampleKey);
    }

    @Override
    public String scriptKill(final byte[] sampleKey) {
        return jedisCluster.scriptKill(sampleKey);
    }

    @Override
    public Long del(final byte[]... keys) {
        return jedisCluster.del(keys);
    }

    @Override
    public List<byte[]> blpop(final int timeout, final byte[]... keys) {
        return jedisCluster.blpop(timeout, keys);
    }

    @Override
    public List<byte[]> brpop(final int timeout, final byte[]... keys) {
        return jedisCluster.brpop(timeout, keys);
    }

    @Override
    public List<byte[]> mget(final byte[]... keys) {
        return jedisCluster.mget(keys);
    }

    @Override
    public String mset(final byte[]... keysvalues) {
        return jedisCluster.mset(keysvalues);
    }

    @Override
    public Long msetnx(final byte[]... keysvalues) {
        return jedisCluster.msetnx(keysvalues);
    }

    @Override
    public String rename(final byte[] oldkey, final byte[] newkey) {
        return jedisCluster.rename(oldkey, newkey);
    }

    @Override
    public Long renamenx(final byte[] oldkey, final byte[] newkey) {
        return jedisCluster.renamenx(oldkey, newkey);
    }

    @Override
    public byte[] rpoplpush(final byte[] srckey, final byte[] dstkey) {
        return jedisCluster.rpoplpush(srckey, dstkey);
    }

    @Override
    public Set<byte[]> sdiff(final byte[]... keys) {
        return jedisCluster.sdiff(keys);
    }

    @Override
    public Long sdiffstore(final byte[] dstkey, final byte[]... keys) {
        return jedisCluster.sdiffstore(dstkey, keys);
    }

    @Override
    public Set<byte[]> sinter(final byte[]... keys) {
        return jedisCluster.sinter(keys);
    }

    @Override
    public Long sinterstore(final byte[] dstkey, final byte[]... keys) {
        return jedisCluster.sinterstore(dstkey, keys);
    }

    @Override
    public Long smove(final byte[] srckey, final byte[] dstkey, final byte[] member) {
        return jedisCluster.smove(srckey, dstkey, member);
    }

    @Override
    public Long sort(final byte[] key, final SortingParams sortingParameters, final byte[] dstkey) {
        return jedisCluster.sort(key, sortingParameters, dstkey);
    }

    @Override
    public Long sort(final byte[] key, final byte[] dstkey) {
        return jedisCluster.sort(key, dstkey);
    }

    @Override
    public Set<byte[]> sunion(final byte[]... keys) {
        return jedisCluster.sunion(keys);
    }

    @Override
    public Long sunionstore(final byte[] dstkey, final byte[]... keys) {
        return jedisCluster.sunionstore(dstkey, keys);
    }

    @Override
    public Long zinterstore(final byte[] dstkey, final byte[]... sets) {
        return jedisCluster.zinterstore(dstkey, sets);
    }

    @Override
    public Long zinterstore(final byte[] dstkey, final ZParams params, final byte[]... sets) {
        return jedisCluster.zinterstore(dstkey, params, sets);
    }

    @Override
    public Long zunionstore(final byte[] dstkey, final byte[]... sets) {
        return jedisCluster.zunionstore(dstkey, sets);
    }

    @Override
    public Long zunionstore(final byte[] dstkey, final ZParams params, final byte[]... sets) {
        return jedisCluster.zunionstore(dstkey, params, sets);
    }

    @Override
    public byte[] brpoplpush(final byte[] source, final byte[] destination, final int timeout) {
        return jedisCluster.brpoplpush(source, destination, timeout);
    }

    @Override
    public Long publish(final byte[] channel, final byte[] message) {
        return jedisCluster.publish(channel, message);
    }

    @Override
    public void subscribe(final BinaryJedisPubSub jedisPubSub, final byte[]... channels) {
        jedisCluster.subscribe(jedisPubSub, channels);
    }

    @Override
    public void psubscribe(final BinaryJedisPubSub jedisPubSub, final byte[]... patterns) {
        jedisCluster.psubscribe(jedisPubSub, patterns);
    }

    @Override
    public Long bitop(final BitOP op, final byte[] destKey, final byte[]... srcKeys) {
        return jedisCluster.bitop(op, destKey, srcKeys);
    }

    @Override
    public String pfmerge(final byte[] destkey, final byte[]... sourcekeys) {
        return jedisCluster.pfmerge(destkey, sourcekeys);
    }

    @Override
    public Long pfcount(final byte[]... keys) {
        return jedisCluster.pfcount(keys);
    }

    @Override
    public Long geoadd(final byte[] key, final double longitude, final double latitude, final byte[] member) {
        return jedisCluster.geoadd(key, longitude, latitude, member);
    }

    @Override
    public Long geoadd(final byte[] key, final Map<byte[], GeoCoordinate> memberCoordinateMap) {
        return jedisCluster.geoadd(key, memberCoordinateMap);
    }

    @Override
    public Double geodist(final byte[] key, final byte[] member1, final byte[] member2) {
        return jedisCluster.geodist(key, member1, member2);
    }

    @Override
    public Double geodist(final byte[] key, final byte[] member1, final byte[] member2, final GeoUnit unit) {
        return jedisCluster.geodist(key, member1, member2, unit);
    }

    @Override
    public List<byte[]> geohash(final byte[] key, final byte[]... members) {
        return jedisCluster.geohash(key, members);
    }

    @Override
    public List<GeoCoordinate> geopos(final byte[] key, final byte[]... members) {
        return jedisCluster.geopos(key, members);
    }

    @Override
    public List<GeoRadiusResponse> georadius(final byte[] key, final double longitude, final double latitude, final double radius,
            final GeoUnit unit) {
        return jedisCluster.georadius(key, longitude, latitude, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(final byte[] key, final double longitude, final double latitude,
            final double radius, final GeoUnit unit) {
        return jedisCluster.georadiusReadonly(key, longitude, latitude, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadius(final byte[] key, final double longitude, final double latitude, final double radius,
            final GeoUnit unit, final GeoRadiusParam param) {
        return jedisCluster.georadius(key, longitude, latitude, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(final byte[] key, final double longitude, final double latitude,
            final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        return jedisCluster.georadiusReadonly(key, longitude, latitude, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(final byte[] key, final byte[] member, final double radius,
            final GeoUnit unit) {
        return jedisCluster.georadiusByMember(key, member, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(final byte[] key, final byte[] member, final double radius,
            final GeoUnit unit) {
        return jedisCluster.georadiusByMemberReadonly(key, member, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(final byte[] key, final byte[] member, final double radius,
            final GeoUnit unit, final GeoRadiusParam param) {
        return jedisCluster.georadiusByMember(key, member, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(final byte[] key, final byte[] member, final double radius,
            final GeoUnit unit, final GeoRadiusParam param) {
        return jedisCluster.georadiusByMemberReadonly(key, member, radius, unit, param);
    }

    @Override
    public Set<byte[]> keys(final byte[] pattern) {
        return jedisCluster.keys(pattern);
    }

    @Override
    public ScanResult<byte[]> scan(final byte[] cursor, final ScanParams params) {
        return jedisCluster.scan(cursor, params);
    }

    @Override
    public ScanResult<Map.Entry<byte[], byte[]>> hscan(final byte[] key, final byte[] cursor) {
        return jedisCluster.hscan(key, cursor);
    }

    @Override
    public ScanResult<Map.Entry<byte[], byte[]>> hscan(final byte[] key, final byte[] cursor, final ScanParams params) {
        return jedisCluster.hscan(key, cursor, params);
    }

    @Override
    public ScanResult<byte[]> sscan(final byte[] key, final byte[] cursor) {
        return jedisCluster.sscan(key, cursor);
    }

    @Override
    public ScanResult<byte[]> sscan(final byte[] key, final byte[] cursor, final ScanParams params) {
        return jedisCluster.sscan(key, cursor, params);
    }

    @Override
    public ScanResult<Tuple> zscan(final byte[] key, final byte[] cursor) {
        return jedisCluster.zscan(key, cursor);
    }

    @Override
    public ScanResult<Tuple> zscan(final byte[] key, final byte[] cursor, final ScanParams params) {
        return jedisCluster.zscan(key, cursor, params);
    }

    @Override
    public List<Long> bitfield(final byte[] key, final byte[]... arguments) {
        return jedisCluster.bitfield(key, arguments);
    }

    @Override
    public Long hstrlen(final byte[] key, final byte[] field) {
        return jedisCluster.hstrlen(key, field);
    }

    @Override
    public byte[] xadd(final byte[] key, final byte[] id, final Map<byte[], byte[]> hash, final long maxLen,
            final boolean approximateLength) {
        return jedisCluster.xadd(key, id, hash, maxLen, approximateLength);
    }

    @Override
    public Long xlen(final byte[] key) {
        return jedisCluster.xlen(key);
    }

    @Override
    public List<byte[]> xrange(final byte[] key, final byte[] start, final byte[] end, final long count) {
        return jedisCluster.xrange(key, start, end, count);
    }

    @Override
    public List<byte[]> xrevrange(final byte[] key, final byte[] end, final byte[] start, final int count) {
        return jedisCluster.xrevrange(key, end, start, count);
    }

    @Override
    public List<byte[]> xread(final int count, final long block, final Map<byte[], byte[]> streams) {
        return jedisCluster.xread(count, block, streams);
    }

    @Override
    public Long xack(final byte[] key, final byte[] group, final byte[]... ids) {
        return jedisCluster.xack(key, group, ids);
    }

    @Override
    public String xgroupCreate(final byte[] key, final byte[] consumer, final byte[] id, final boolean makeStream) {
        return jedisCluster.xgroupCreate(key, consumer, id, makeStream);
    }

    @Override
    public String xgroupSetID(final byte[] key, final byte[] consumer, final byte[] id) {
        return jedisCluster.xgroupSetID(key, consumer, id);
    }

    @Override
    public Long xgroupDestroy(final byte[] key, final byte[] consumer) {
        return jedisCluster.xgroupDestroy(key, consumer);
    }

    @Override
    public String xgroupDelConsumer(final byte[] key, final byte[] consumer, final byte[] consumerName) {
        return jedisCluster.xgroupDelConsumer(key, consumer, consumerName);
    }

    @Override
    public List<byte[]> xreadGroup(final byte[] groupname, final byte[] consumer, final int count, final long block,
            final boolean noAck, final Map<byte[], byte[]> streams) {
        return jedisCluster.xreadGroup(groupname, consumer, count, block, noAck, streams);
    }

    @Override
    public Long xdel(final byte[] key, final byte[]... ids) {
        return jedisCluster.xdel(key, ids);
    }

    @Override
    public Long xtrim(final byte[] key, final long maxLen, final boolean approximateLength) {
        return jedisCluster.xtrim(key, maxLen, approximateLength);
    }

    @Override
    public List<byte[]> xpending(final byte[] key, final byte[] groupname, final byte[] start, final byte[] end, final int count,
            final byte[] consumername) {
        return jedisCluster.xpending(key, groupname, start, end, count, consumername);
    }

    @Override
    public List<byte[]> xclaim(final byte[] key, final byte[] groupname, final byte[] consumername, final long minIdleTime,
            final long newIdleTime, final int retries, final boolean force, final byte[][] ids) {
        return jedisCluster.xclaim(key, groupname, consumername, minIdleTime, newIdleTime, retries, force, ids);
    }

    @Override
    public Long waitReplicas(final byte[] key, final int replicas, final long timeout) {
        return jedisCluster.waitReplicas(key, replicas, timeout);
    }

    public Object sendCommand(final byte[] sampleKey, final ProtocolCommand cmd, final byte[]... args) {
        return jedisCluster.sendCommand(sampleKey, cmd, args);
    }

}
