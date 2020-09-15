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
package com.wl4g.components.core.utils.expression;

import static com.wl4g.components.common.lang.Assert2.hasTextOf;

import java.util.ArrayList;

import javax.validation.constraints.NotBlank;

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.ParserContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.wl4g.components.common.annotation.Nullable;

/**
 * {@link SpelExpressions}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-09-15
 * @sine v1.0.0
 * @see
 */
public abstract class SpelExpressions {

	/**
	 * Resolving spring expression to real value.
	 * 
	 * @param expression
	 * @param model
	 * @return
	 */
	@SuppressWarnings("serial")
	public static Object resolve(@NotBlank String expression, @Nullable Object model) {
		hasTextOf(expression, "expression");

		// Create expression parser.
		StandardEvaluationContext context = new StandardEvaluationContext(model);
		context.setPropertyAccessors(new ArrayList<PropertyAccessor>() {
			{
				add(new MapAccessor());
				add(new ReflectivePropertyAccessor());
			}
		});
		return defaultParser.parseExpression(expression, ParserContext.TEMPLATE_EXPRESSION).getValue(context);
	}

	/** {@link ExpressionParser} */
	private static final ExpressionParser defaultParser = new SpelExpressionParser() {
		@Override
		protected SpelExpression doParseExpression(String expressionString, ParserContext context) throws ParseException {
			return new AliasInternalSpelExpressionParser(new SpelParserConfiguration()).doParseExpression(expressionString,
					context);
		}
	};

}
