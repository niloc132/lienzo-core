/*
   Copyright (c) 2014,2015,2016 Ahome' Innovation Technologies. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ait.lienzo.tools.client.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import elemental2.core.JsArray;
import elemental2.core.JsIterable;
import elemental2.core.JsIteratorIterable;
import elemental2.core.JsMap;
import elemental2.core.JsMap.EntriesJsIteratorIterableTypeParameterArrayUnionType;
import elemental2.core.JsMap.JsIterableTypeParameterArrayUnionType;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

public final class NFastStringHistogram //implements NHasJSO<NFastStringHistogram.NFastStringHistogramJSO>, NJSONStringify
{
    private final NFastStringHistogramJSO m_jso = NFastStringHistogramJSO.make();

    public NFastStringHistogram()
    {
    }

    public NFastStringHistogram(final String key)
    {
        inc(key);
    }

    public NFastStringHistogram(final String key, final String... keys)
    {
        inc(key, keys);
    }

    public NFastStringHistogram(final Iterable<String> keys)
    {
        inc(keys);
    }

    public final NFastStringHistogram inc(final String key)
    {
        if (null != key)
        {
            m_jso.inc(key);
        }
        return this;
    }

// @FIXME (mdp)
//    public final JSONObject toJSONObject()
//    {
//        return new JSONObject(m_jso);
//    }

    public final NFastStringHistogram inc(final String key, final String... keys)
    {
        inc(key);

        for (String k : keys)
        {
            inc(k);
        }
        return this;
    }

    public final NFastStringHistogram inc(final Iterable<String> keys)
    {
        for (String k : keys)
        {
            inc(k);
        }
        return this;
    }

    public final NFastStringHistogram dec(final String key)
    {
        if (null != key)
        {
            m_jso.dec(key);
        }
        return this;
    }

    public final NFastStringHistogram dec(final String key, final String... keys)
    {
        dec(key);

        for (String k : keys)
        {
            dec(k);
        }
        return this;
    }

    public final NFastStringHistogram dec(final Iterable<String> keys)
    {
        for (String k : keys)
        {
            dec(k);
        }
        return this;
    }

    public final boolean contains(final String key)
    {
        if (null != key)
        {
            return m_jso.has(key);
        }
        return false;
    }

    public final int total()
    {
        return m_jso.total();
    }

    public final int total(final String key)
    {
        if (null != key)
        {
            return m_jso.total(key);
        }
        return 0;
    }

    public final <T extends Collection<String>> T into(final T coll)
    {
        coll.addAll(keys());

        return coll;
    }

    public final Collection<String> keys()
    {
        return Js.uncheckedCast(m_jso.keys());
    }

    public final int size()
    {
        return m_jso.size;
    }

    public final NFastStringHistogram clear()
    {
        m_jso.clear();

        return this;
    }

    public final boolean isEmpty()
    {
        return m_jso.isEmpty();
    }

// @FIXME (mdp)
//    @Override
//    public final String toJSONString(final NJSONReplacer replacer, final int indent)
//    {
//        return JSON.toJSONString(m_jso, replacer, indent);
//    }
//
//    @Override
//    public final String toJSONString(final NJSONReplacer replacer, final String indent)
//    {
//        return JSON.toJSONString(m_jso, replacer, indent);
//    }
//
//    @Override
//    public final String toJSONString(final NJSONReplacer replacer)
//    {
//        return JSON.toJSONString(m_jso, replacer);
//    }
//
//    @Override
//    public final String toJSONString(final int indent)
//    {
//        return JSON.toJSONString(m_jso, indent);
//    }
//
//    @Override
//    public final String toJSONString(final String indent)
//    {
//        return JSON.toJSONString(m_jso, indent);
//    }
//
//    @Override
//    public final String toJSONString()
//    {
//        return JSON.toJSONString(m_jso);
//    }
//
//    @Override
//    public final String toString()
//    {
//        return toJSONString();
//    }
//
//    @Override
//    public final NFastStringHistogramJSO getJSO()
//    {
//        return m_jso;
//    }

//    @Override
//    public final NObject onWire()
//    {
//        final NObjectJSO jso = m_jso.cast();
//
//        return new NObject(jso);
//    }

    @JsType(isNative = true, name = "Map", namespace = JsPackage.GLOBAL)
    public static final class NFastStringHistogramJSO implements JsIterable<JsIterableTypeParameterArrayUnionType<String, Integer>[]>
    {
        protected NFastStringHistogramJSO()
        {
        }

        @JsOverlay
        static final NFastStringHistogramJSO make()
        {
            return new NFastStringHistogramJSO();
        }

        public int size;

        public native final Integer get(final String key);

        public native JsMap<String, Integer> set(String key, Integer value);

        public native void clear();

        public native boolean delete(String key);

        public native JsIteratorIterable<
                EntriesJsIteratorIterableTypeParameterArrayUnionType<String, Integer>[]>
        entries();

        public native Object forEach(
                JsMap.ForEachCallbackFn<? super String, ? super Integer> callback, Object thisArg);

        public native Object forEach(JsMap.ForEachCallbackFn<? super String, ? super Integer> callback);

        public native boolean has(String key);

        public native JsIteratorIterable<String> keys();

        public native JsIteratorIterable<Integer> values();

        @JsOverlay
        private final void inc(String key)
        {
            int val;
			if (has(key)) {
			    val = get(key);
			} else {
			    val = 1;
			}
            set(key, val);
        }

        @JsOverlay
        private final void dec(String key)
        {
            int val = get(key);
            val = val - 1;
            if (val < 1) {
                delete(key);
            } else {
                set(key, val);
            }
        }


        @JsOverlay
        private final int total()
        {
			int     i = 0;
			Integer[] vales = Js.uncheckedCast(values());
			for ( int val :  vales)
            {
                i = i + val;
            }
			return i;
        }

        @JsOverlay
        private final int total(String key)
        {
            int val = 0;
			if (this.has(key)) {
                val = get(key);
			}
			return val;
        }

        @JsOverlay
        private final boolean isEmpty()
        {
			return size == 0;
        }
    }
}
