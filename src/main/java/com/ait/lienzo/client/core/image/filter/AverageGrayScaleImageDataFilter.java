/*
   Copyright (c) 2017 Ahome' Innovation Technologies. All rights reserved.

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

package com.ait.lienzo.client.core.image.filter;

import com.ait.lienzo.client.core.shape.json.IFactory;
import com.ait.lienzo.client.core.shape.json.validators.ValidationContext;
import com.ait.lienzo.client.core.shape.json.validators.ValidationException;
import com.ait.lienzo.client.core.types.ImageDataUtil;
import com.ait.lienzo.shared.core.types.ImageFilterType;

import elemental2.core.JsArray;
import elemental2.core.Uint8ClampedArray;
import elemental2.dom.ImageData;
import jsinterop.base.Js;
import jsinterop.base.JsArrayLike;

/**
 * A class that allows for easy creation of Gray Scale Filters.
 */
public class AverageGrayScaleImageDataFilter extends AbstractImageDataFilter<AverageGrayScaleImageDataFilter>
{
    public AverageGrayScaleImageDataFilter()
    {
        super(ImageFilterType.AverageGrayScaleImageDataFilterType);
    }

    protected AverageGrayScaleImageDataFilter(Object node, ValidationContext ctx) throws ValidationException
    {
        super(ImageFilterType.AverageGrayScaleImageDataFilterType, node, ctx);
    }

    @Override
    public ImageData filter(ImageData source, boolean copy)
    {
        if (null == source)
        {
            return null;
        }
        if (copy)
        {
            source = ImageDataUtil.copy(source);
        }
        if (false == isActive())
        {
            return source;
        }
        final Uint8ClampedArray data = source.data;

        if (null == data)
        {
            return source;
        }
        filter_(data, FilterCommonOps.getLength(source));

        return source;
    }

    private final void filter_(Uint8ClampedArray dataArray, int length)
    {
//        int[] data = Uint8ClampedArray.ConstructorLengthUnionType.of(dataArray).asIntArray();
        int[] data = Js.uncheckedCast(dataArray);
        for (int i = 0; i < length; i += 4)
        {
            double v = Js.coerceToInt(((data[i] + data[i + 1] + data[i + 2]) / 3.0) + 0.5);
            data[i] = data[i + 1] = data[i + 2] = (int) v;
        }
    };

    @Override
    public IFactory<AverageGrayScaleImageDataFilter> getFactory()
    {
        return new AverageGrayScaleImageDataFilterFactory();
    }

    public static class AverageGrayScaleImageDataFilterFactory extends ImageDataFilterFactory<AverageGrayScaleImageDataFilter>
    {
        public AverageGrayScaleImageDataFilterFactory()
        {
            super(ImageFilterType.AverageGrayScaleImageDataFilterType);
        }

        @Override
        public AverageGrayScaleImageDataFilter create(Object node, ValidationContext ctx) throws ValidationException
        {
            return new AverageGrayScaleImageDataFilter(node, ctx);
        }
    }
}
