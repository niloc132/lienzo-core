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

import elemental2.core.JsArray;
import elemental2.core.JsIterable;
import elemental2.core.JsIteratorIterable;
import elemental2.core.JsSet;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

@JsType(isNative = true, name = "Set", namespace = JsPackage.GLOBAL)
public final class NFastStringSet implements JsIterable<String>//, NHasJSO<NFastStringSet.NFastStringSetJSO>, NJSONStringify
{
    //private final NFastStringSetJSO m_jso;

//    public NFastStringSet(final NFastStringSetJSO jso)
//    {
//        m_jso = ((null == jso) ? NFastStringSetJSO.makeFromString() : jso);
//    }
//
//    public NFastStringSet()
//    {
//        m_jso = NFastStringSetJSO.makeFromString();
//    }

    @JsOverlay
    public static final NFastStringSet makeFromString(final String key)
    {
        NFastStringSet set = new NFastStringSet();
        set.add(key);
        return set;
    }

    @JsOverlay
    public static final NFastStringSet makeFromSet(final NFastStringSet keys)
    {
        NFastStringSet set = new NFastStringSet();
        String[] keyArray = Js.uncheckedCast(keys);
        for(String key : keyArray)
        {
            set.add(key);
        }
        return set;
    }
//
//    public NFastStringSet(final String key, final String... keys)
//    {
//        this();
//
//        add(key, keys);
//    }
//
//    public NFastStringSet(final Iterable<String> keys)
//    {
//        this();
//
//        add(keys);
//    }
//
//    public NFastStringSet(final NFastStringSet nset)
//    {
//        this();
//
//        add(nset);
//    }
//
//    public final JSONObject toJSONObject()
//    {
//        return new JSONObject(m_jso);
//    }
//
//    @Override
//    public final NFastStringSetJSO getJSO()
//    {
//        return m_jso;
//    }

    public int size;

//    public JsSet() {}
//
//    public JsSet(JsSet.ConstructorIterableUnionType<VALUE> iterable) {}
//
//    public JsSet(JsIterable<VALUE> iterable) {}
//
//    public JsSet(VALUE[] iterable) {}

    //--
    public native NFastStringSet add(String value);

    public native boolean delete(String value);

    public native JsIteratorIterable<String[]> entries();

    public native Object forEach(JsSet.ForEachCallbackFn<? super String> callback, Object thisArg);

    public native Object forEach(JsSet.ForEachCallbackFn<? super String> callback);

    public native boolean has(String value);

    public native JsIteratorIterable<String> keys();

    public native JsIteratorIterable<String> values();

//    public final NFastStringSet add(final String key)
//    {
//        m_jso.add(Objects.requireNonNull(key));
//
//        return this;
//    }

//    public final NFastStringSet add(final String key, final String... keys)
//    {
//        add(key);
//
//        for (String k : keys)
//        {
//            add(k);
//        }
//        return this;
//    }
//
//    public final NFastStringSet add(final Iterable<String> keys)
//    {
//        for (String key : keys)
//        {
//            add(key);
//        }
//        return this;
//    }
//
//    public final NFastStringSet add(final NFastStringSet nset)
//    {
//        m_jso.add(Objects.requireNonNull(nset).m_jso);
//
//        return this;
//    }

    @JsOverlay
    public final boolean contains(final String key)
    {
        return has(key);
    }

    @JsOverlay
    public final NFastStringSet remove(final String key)
    {
        delete(key);
        return this;
    }

//    public final Collection<String> keys()
//    {
//        final ArrayList<String> keys = new ArrayList<String>();
//
//        m_jso.keys_0(keys);
//
//        return Collections.unmodifiableList(keys);
//    }

    @JsOverlay
    public final int size()
    {
        return size;
    }

    public native void clear();

    @JsOverlay
    public final boolean isEmpty()
    {
        return size == 0;
    }

//    @JsOverlay
//    public final Iterator<String> iterator()
//    {
//        return JsArray.FromArrayLikeUnionType.of(this).asJsArrayLike().asList().iterator();
//    }

//    @Override
//    public final String toString()
//    {
//        return toJSONString();
//    }
//
//    @Override
//    public final String toJSONString(final NJSONReplacer replacer, final int indent)
//    {
//        return NUtils.JSON.toJSONString(m_jso, replacer, indent);
//    }
//
//    @Override
//    public final String toJSONString(final NJSONReplacer replacer, final String indent)
//    {
//        return NUtils.JSON.toJSONString(m_jso, replacer, indent);
//    }
//
//    @Override
//    public final String toJSONString(final NJSONReplacer replacer)
//    {
//        return NUtils.JSON.toJSONString(m_jso, replacer);
//    }
//
//    @Override
//    public final String toJSONString(final int indent)
//    {
//        return NUtils.JSON.toJSONString(m_jso, indent);
//    }
//
//    @Override
//    public final String toJSONString(final String indent)
//    {
//        return NUtils.JSON.toJSONString(m_jso, indent);
//    }
//
//    @Override
//    public final String toJSONString()
//    {
//        return NUtils.JSON.toJSONString(m_jso);
//    }
//
//    @Override
//    public final NObject onWire()
//    {
//        final NObjectJSO jso = m_jso.cast();
//
//        return new NObject(jso);
//    }

//    public final <T extends Collection<String>> T into(final T coll)
//    {
//        coll.addAll(keys());
//
//        return coll;
//    }

    @JsOverlay
    public final boolean any(final NFastStringSet look)
    {
        if (null == look)
        {
            return false;
        }

        String[] array = Js.uncheckedCast(look);
        for ( String name : array)
        {
            if (look.contains(name) && contains(name))
            {
                return true;
            }
        }
        return false;
    }

    @JsOverlay
    public final boolean none(final NFastStringSet look)
    {
        String[] array = Js.uncheckedCast(look);
        for ( String name : array) {
            if (look.contains(name) && contains(name))
            {
                return false;
            }
        }
        return true;
    }

    @JsOverlay
    public final boolean all(final NFastStringSet look)
    {
        boolean sean = false;
        String[] array = Js.uncheckedCast(look);
        for ( String name : array) {
            if (contains(name))
            {
                sean = true;
            }
            else
            {
                return false;
            }
        }
        return sean;
    }
}
