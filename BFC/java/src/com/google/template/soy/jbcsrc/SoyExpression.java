/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.template.soy.jbcsrc;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.template.soy.jbcsrc.BytecodeUtils.OBJECT;
import static com.google.template.soy.jbcsrc.BytecodeUtils.SOY_LIST_TYPE;
import static com.google.template.soy.jbcsrc.BytecodeUtils.SOY_VALUE_TYPE;
import static com.google.template.soy.jbcsrc.BytecodeUtils.constant;
import static com.google.template.soy.jbcsrc.BytecodeUtils.logicalNot;

import com.google.protobuf.Message;
import com.google.template.soy.base.SourceLocation;
import com.google.template.soy.base.internal.SanitizedContentKind;
import com.google.template.soy.data.SanitizedContent.ContentKind;
import com.google.template.soy.data.SoyValue;
import com.google.template.soy.data.SoyValueProvider;
import com.google.template.soy.soytree.CallNode;
import com.google.template.soy.soytree.MsgNode;
import com.google.template.soy.soytree.PrintNode;
import com.google.template.soy.types.SoyType;
import com.google.template.soy.types.SoyType.Kind;
import com.google.template.soy.types.aggregate.ListType;
import com.google.template.soy.types.primitive.BoolType;
import com.google.template.soy.types.primitive.FloatType;
import com.google.template.soy.types.primitive.IntType;
import com.google.template.soy.types.primitive.NullType;
import com.google.template.soy.types.primitive.SanitizedType;
import com.google.template.soy.types.primitive.StringType;
import com.google.template.soy.types.primitive.UnknownType;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * An Expression involving a soy value.
 *
 * <p>SoyExpressions can be {@link #box() boxed} into SoyValue subtypes and they also support some
 * implicit conversions.
 *
 * <p>All soy expressions are convertible to {@code boolean} or {@link String} valued expressions,
 * but depending on the type they may also support additional unboxing conversions.
 */
final class SoyExpression extends Expression {

  static SoyExpression forSoyValue(SoyType type, Expression delegate) {
    return new SoyExpression(SoyRuntimeType.getBoxedType(type), delegate);
  }

  static SoyExpression forBool(Expression delegate) {
    return new SoyExpression(getUnboxedType(BoolType.getInstance()), delegate);
  }

  static SoyExpression forFloat(Expression delegate) {
    return new SoyExpression(getUnboxedType(FloatType.getInstance()), delegate);
  }

  static SoyExpression forInt(Expression delegate) {
    return new SoyExpression(getUnboxedType(IntType.getInstance()), delegate);
  }

  static SoyExpression forString(Expression delegate) {
    return new SoyExpression(getUnboxedType(StringType.getInstance()), delegate);
  }

  static SoyExpression forSanitizedString(Expression delegate, SanitizedContentKind kind) {
    return new SoyExpression(getUnboxedType(SanitizedType.getTypeForContentKind(kind)), delegate);
  }

  static SoyExpression forList(ListType listType, Expression delegate) {
    return new SoyExpression(getUnboxedType(listType), delegate);
  }

  static SoyExpression forProto(SoyRuntimeType type, Expression delegate) {
    checkArgument(type.soyType().getKind() == Kind.PROTO);
    return new SoyExpression(type, delegate);
  }

  /**
   * Returns an Expression that evaluates to a list containing all the items as boxed soy values.
   */
  static Expression asBoxedList(List<SoyExpression> items) {
    List<Expression> childExprs = new ArrayList<>(items.size());
    for (SoyExpression child : items) {
      childExprs.add(child.box());
    }
    return BytecodeUtils.asList(childExprs);
  }

  static final SoyExpression NULL =
      new SoyExpression(
          getUnboxedType(NullType.getInstance()),
          new Expression(OBJECT.type(), Feature.CHEAP) {
            @Override
            void doGen(CodeBuilder cb) {
              cb.pushNull();
            }
          });

  static final SoyExpression NULL_BOXED =
      new SoyExpression(
          SoyRuntimeType.getBoxedType(NullType.getInstance()),
          new Expression(SOY_VALUE_TYPE, Feature.CHEAP) {
            @Override
            void doGen(CodeBuilder cb) {
              cb.pushNull();
            }
          });

  static final SoyExpression TRUE = forBool(BytecodeUtils.constant(true));

  static final SoyExpression FALSE = forBool(BytecodeUtils.constant(false));

  private static SoyRuntimeType getUnboxedType(SoyType soyType) {
    return SoyRuntimeType.getUnboxedType(soyType).get();
  }

  private final SoyRuntimeType soyRuntimeType;
  private final Expression delegate;

  private SoyExpression(SoyRuntimeType soyRuntimeType, Expression delegate) {
    super(delegate.resultType(), delegate.features());
    checkArgument(
        BytecodeUtils.isPossiblyAssignableFrom(soyRuntimeType.runtimeType(), delegate.resultType()),
        "Expecting SoyExpression type of %s, found delegate with type of %s",
        soyRuntimeType.runtimeType(),
        delegate.resultType());
    this.soyRuntimeType = soyRuntimeType;
    this.delegate = delegate;
  }

  /** Returns the {@link SoyType} of the expression. */
  final SoyType soyType() {
    return soyRuntimeType.soyType();
  }

  /** Returns the {@link SoyRuntimeType} of the expression. */
  final SoyRuntimeType soyRuntimeType() {
    return soyRuntimeType;
  }

  @Override
  final void doGen(CodeBuilder adapter) {
    delegate.gen(adapter);
  }

  @Override
  SoyExpression withSourceLocation(SourceLocation location) {
    return withSource(delegate.withSourceLocation(location));
  }

  boolean assignableToNullableInt() {
    return soyRuntimeType.assignableToNullableInt();
  }

  boolean assignableToNullableFloat() {
    return soyRuntimeType.assignableToNullableFloat();
  }

  boolean assignableToNullableNumber() {
    return soyRuntimeType.assignableToNullableNumber();
  }

  boolean assignableToNullableString() {
    return soyRuntimeType.assignableToNullableString();
  }

  boolean isBoxed() {
    return soyRuntimeType.isBoxed();
  }

  /** Returns an Expression of a non-null {@link SoyValueProvider} providing this value. */
  Expression boxAsSoyValueProvider() {
    if (soyType().equals(NullType.getInstance())) {
      if (delegate == NULL || delegate == NULL_BOXED) {
        return FieldRef.NULL_PROVIDER.accessor();
      }
      // otherwise this expression might have side effects,  evaluate it as a statement then return
      // the NULL_PROVIDER
      return toStatement().then(FieldRef.NULL_PROVIDER.accessor());
    }
    if (delegate.isNonNullable()) {
      // Every SoyValue is-a SoyValueProvider, so if it is non-null
      return box();
    }
    if (isBoxed()) {
      return new Expression(
          BytecodeUtils.SOY_VALUE_PROVIDER_TYPE, delegate.features().plus(Feature.NON_NULLABLE)) {
        @Override
        void doGen(CodeBuilder adapter) {
          Label end = new Label();
          delegate.gen(adapter);
          adapter.dup();
          adapter.ifNonNull(end);
          adapter.pop();
          FieldRef.NULL_PROVIDER.accessStaticUnchecked(adapter);
          adapter.mark(end);
        }
      };
    }
    return new Expression(
        BytecodeUtils.SOY_VALUE_PROVIDER_TYPE, delegate.features().plus(Feature.NON_NULLABLE)) {
      @Override
      void doGen(CodeBuilder adapter) {
        Label end = new Label();
        delegate.gen(adapter);
        adapter.dup();
        Label nonNull = new Label();
        adapter.ifNonNull(nonNull);
        adapter.pop(); // pop the null value and replace with the nullprovider
        FieldRef.NULL_PROVIDER.accessStaticUnchecked(adapter);
        adapter.goTo(end);
        adapter.mark(nonNull);
        doBox(adapter, soyRuntimeType);
        adapter.mark(end);
      }
    };
  }

  /** Returns a SoyExpression that evaluates to a subtype of {@link SoyValue}. */
  SoyExpression box() {
    if (isBoxed()) {
      return this;
    }
    if (soyType().equals(NullType.getInstance())) {
      if (delegate == NULL) {
        return NULL_BOXED;
      }
      return asBoxed(toStatement().then(NULL_BOXED));
    }
    // since we aren't boxed and these must be primitives so we don't need to worry about
    // nullability
    if (soyRuntimeType.isKnownBool()) {
      return asBoxed(MethodRef.BOOLEAN_DATA_FOR_VALUE.invoke(delegate));
    }
    if (soyRuntimeType.isKnownInt()) {
      return asBoxed(MethodRef.INTEGER_DATA_FOR_VALUE.invoke(delegate));
    }
    if (soyRuntimeType.isKnownFloat()) {
      return asBoxed(MethodRef.FLOAT_DATA_FOR_VALUE.invoke(delegate));
    }
    // If null is expected and it is a reference type we want to propagate null through the boxing
    // operation
    final boolean isNonNullable = delegate.isNonNullable();
    return asBoxed(
        new Expression(soyRuntimeType.box().runtimeType(), features()) {
          @Override
          void doGen(CodeBuilder adapter) {
            Label end = null;
            delegate.gen(adapter);
            if (!isNonNullable) {
              end = new Label();
              BytecodeUtils.nullCoalesce(adapter, end);
            }
            doBox(adapter, soyRuntimeType.asNonNullable());
            if (end != null) {
              adapter.mark(end);
            }
          }
        });
  }

  /**
   * Generates code to box the expression assuming that it is non-nullable and on the top of the
   * stack.
   */
  private static void doBox(CodeBuilder adapter, SoyRuntimeType type) {
    if (type.isKnownSanitizedContent()) {
      FieldRef.enumReference(
              ContentKind.valueOf(((SanitizedType) type.soyType()).getContentKind().name()))
          .accessStaticUnchecked(adapter);
      MethodRef.ORDAIN_AS_SAFE.invokeUnchecked(adapter);
    } else if (type.isKnownString()) {
      MethodRef.STRING_DATA_FOR_VALUE.invokeUnchecked(adapter);
    } else if (type.isKnownList()) {
      MethodRef.LIST_IMPL_FOR_PROVIDER_LIST.invokeUnchecked(adapter);
    } else if (type.isKnownMap()) {
      MethodRef.DICT_IMPL_FOR_PROVIDER_MAP.invokeUnchecked(adapter);
    } else if (type.isKnownProto()) {
      MethodRef.SOY_PROTO_VALUE_IMPL_CREATE.invokeUnchecked(adapter);
    } else {
      throw new IllegalStateException("Can't box soy expression of type " + type);
    }
  }

  private SoyExpression asBoxed(Expression expr) {
    return new SoyExpression(soyRuntimeType.box(), expr);
  }

  /** Coerce this expression to a boolean value. */
  SoyExpression coerceToBoolean() {
    // First deal with primitives which don't have to care about null.
    if (BytecodeUtils.isPrimitive(resultType())) {
      return coercePrimitiveToBoolean();
    }
    if (soyType().equals(NullType.getInstance())) {
      return FALSE;
    }
    if (delegate.isNonNullable()) {
      return coerceNonNullableReferenceTypeToBoolean();
    } else {
      // If we are potentially nullable, then map null to false and run the normal logic recursively
      // for the non-nullable branch.
      final Label end = new Label();
      return withSource(
              new Expression(delegate.resultType(), delegate.features()) {
                @Override
                void doGen(CodeBuilder adapter) {
                  delegate.gen(adapter);
                  adapter.dup();
                  Label nonNull = new Label();
                  adapter.ifNonNull(nonNull);
                  adapter.pop();
                  adapter.pushBoolean(false);
                  adapter.goTo(end);
                  adapter.mark(nonNull);
                }
              })
          .asNonNullable()
          .coerceToBoolean()
          .labelEnd(end);
    }
  }

  private SoyExpression coercePrimitiveToBoolean() {
    if (resultType().equals(Type.BOOLEAN_TYPE)) {
      return this;
    } else if (resultType().equals(Type.DOUBLE_TYPE)) {
      return forBool(MethodRef.RUNTIME_COERCE_DOUBLE_TO_BOOLEAN.invoke(delegate));
    } else if (resultType().equals(Type.LONG_TYPE)) {
      return forBool(BytecodeUtils.compare(Opcodes.IFNE, delegate, BytecodeUtils.constant(0L)));
    } else {
      throw new AssertionError(
          "resultType(): " + resultType() + " is not a valid type for a SoyExpression");
    }
  }

  private SoyExpression coerceNonNullableReferenceTypeToBoolean() {
    if (isBoxed()) {
      // If we are boxed, just call the SoyValue method
      return forBool(delegate.invoke(MethodRef.SOY_VALUE_COERCE_TO_BOOLEAN));
    }
    // unboxed non-primitive types.  This would be strings, protos or lists
    if (soyRuntimeType.isKnownString()) {
      return forBool(logicalNot(delegate.invoke(MethodRef.STRING_IS_EMPTY)));
    }
    // All other types are always truthy, but we still need to eval the delegate in case it has
    // side effects or contains a null exit branch.
    return forBool(
        new Expression(Type.BOOLEAN_TYPE, delegate.features()) {
          @Override
          void doGen(CodeBuilder adapter) {
            delegate.gen(adapter);
            adapter.pop();
            adapter.pushBoolean(true);
          }
        });
  }

  /** Coerce this expression to a string value. */
  SoyExpression coerceToString() {
    if (soyRuntimeType.isKnownString() && !isBoxed()) {
      return this;
    }
    if (BytecodeUtils.isPrimitive(resultType())) {
      if (resultType().equals(Type.BOOLEAN_TYPE)) {
        return forString(MethodRef.BOOLEAN_TO_STRING.invoke(delegate));
      } else if (resultType().equals(Type.DOUBLE_TYPE)) {
        return forString(MethodRef.DOUBLE_TO_STRING.invoke(delegate));
      } else if (resultType().equals(Type.LONG_TYPE)) {
        return forString(MethodRef.LONG_TO_STRING.invoke(delegate));
      } else {
        throw new AssertionError(
            "resultType(): " + resultType() + " is not a valid type for a SoyExpression");
      }
    }
    if (!isBoxed()) {
      // this is for unboxed reference types (strings, lists, protos) String.valueOf handles null
      // implicitly
      return forString(MethodRef.STRING_VALUE_OF.invoke(delegate));
    }
    return forString(MethodRef.RUNTIME_COERCE_TO_STRING.invoke(delegate));
  }

  /** Coerce this expression to a double value. Useful for float-int comparisons. */
  SoyExpression coerceToDouble() {
    if (!isBoxed()) {
      if (soyRuntimeType.isKnownFloat()) {
        return this;
      }
      if (soyRuntimeType.isKnownInt()) {
        return forFloat(BytecodeUtils.numericConversion(delegate, Type.DOUBLE_TYPE));
      }
      throw new UnsupportedOperationException("Can't convert " + resultType() + " to a double");
    }
    if (soyRuntimeType.isKnownFloat()) {
      return forFloat(delegate.invoke(MethodRef.SOY_VALUE_FLOAT_VALUE));
    }
    return forFloat(delegate.invoke(MethodRef.SOY_VALUE_NUMBER_VALUE));
  }

  // TODO(lukes): split this into a set of specialized methods, one per target type like we did
  // for the 'coerce' methods.

  /**
   * Unboxes this to a {@link SoyExpression} with a runtime type of {@code asType}.
   *
   * <p>This method is appropriate when you know (likely via inspection of the {@link #soyType()},
   * or other means) that the value does have the appropriate type but you prefer to interact with
   * it as its unboxed representation. If you simply want to 'coerce' the given value to a new type
   * consider {@link #coerceToBoolean()} {@link #coerceToDouble()} or {@link #coerceToString()}
   * which are designed for that use case.
   */
  SoyExpression unboxAs(Class<?> asType) {
    checkArgument(
        !SoyValue.class.isAssignableFrom(asType),
        "Cannot use unboxAs() to convert to a SoyValue: %s, use .box() instead",
        asType);

    // No-op conversion, always allow.
    // SoyExpressions that are already unboxed fall into this case.
    if (BytecodeUtils.isDefinitelyAssignableFrom(
        Type.getType(asType), soyRuntimeType.runtimeType())) {
      return this;
    }

    // Attempting to unbox an unboxed proto
    if (asType.equals(Message.class) && soyRuntimeType.isKnownProto() && !isBoxed()) {
      return this;
    }

    if (!isBoxed()) {
      throw new IllegalStateException(
          "Trying to unbox an unboxed value ("
              + soyRuntimeType
              + ") into "
              + asType
              + " doesn't make sense. Should you be using a type coercion? e.g. coerceToBoolean()");
    }

    if (asType.equals(boolean.class)) {
      return forBool(delegate.invoke(MethodRef.SOY_VALUE_BOOLEAN_VALUE));
    }
    if (asType.equals(long.class)) {
      return forInt(delegate.invoke(MethodRef.SOY_VALUE_LONG_VALUE));
    }
    if (asType.equals(double.class)) {
      return forFloat(delegate.invoke(MethodRef.SOY_VALUE_FLOAT_VALUE));
    }

    if (delegate.isNonNullable()) {
      if (asType.equals(String.class)) {
        Expression unboxedString = delegate.invoke(MethodRef.SOY_VALUE_STRING_VALUE);
        // We need to ensure that santized types don't lose their content kinds
        return soyRuntimeType.isKnownSanitizedContent()
            ? forSanitizedString(unboxedString, ((SanitizedType) soyType()).getContentKind())
            : forString(unboxedString);
      }
      if (asType.equals(List.class)) {
        return unboxAsList();
      }
      if (asType.equals(Message.class)) {
        SoyRuntimeType runtimeType = getUnboxedType(soyType());
        return forProto(
            runtimeType,
            delegate
                .invoke(MethodRef.SOY_PROTO_VALUE_GET_PROTO)
                .checkedCast(runtimeType.runtimeType()));
      }
    } else {
      // else it must be a List/Proto/String all of which must preserve null through the unboxing
      // operation
      // TODO(lukes): this violates the expression contract since we jump to a label outside the
      // scope of the expression
      final Label end = new Label();
      Expression nonNullDelegate =
          new Expression(resultType(), features()) {
            @Override
            void doGen(CodeBuilder adapter) {
              delegate.gen(adapter);
              BytecodeUtils.nullCoalesce(adapter, end);
            }
          };
      return withSource(nonNullDelegate).asNonNullable().unboxAs(asType).asNullable().labelEnd(end);
    }
    throw new UnsupportedOperationException("Can't unbox " + soyRuntimeType + " as " + asType);
  }

  private SoyExpression unboxAsList() {
    ListType asListType;
    if (soyRuntimeType.isKnownList()) {
      asListType = (ListType) soyType();
    } else {
      Kind kind = soyType().getKind();
      if (kind == Kind.UNKNOWN) {
        asListType = ListType.of(UnknownType.getInstance());
      } else {
        // The type checker should have already rejected all of these
        throw new UnsupportedOperationException("Can't convert " + soyRuntimeType + " to List");
      }
    }
    return forList(
        asListType, delegate.checkedCast(SOY_LIST_TYPE).invoke(MethodRef.SOY_LIST_AS_JAVA_LIST));
  }

  /** Returns a new {@link SoyExpression} with the same type but a new delegate expression. */
  SoyExpression withSource(Expression expr) {
    if (expr == delegate) {
      return this;
    }
    return new SoyExpression(soyRuntimeType, expr);
  }

  /**
   * Applies a print directive to the soyValue, only useful for parameterless print directives such
   * as those applied to {@link MsgNode msg nodes} and {@link CallNode call nodes} for autoescaping.
   * For {@link PrintNode print nodes}, the directives may be parameterized by arbitrary soy
   * expressions.
   */
  SoyExpression applyPrintDirective(Expression renderContext, String directive) {
    return applyPrintDirective(renderContext, directive, MethodRef.IMMUTABLE_LIST_OF.invoke());
  }

  /** Applies a print directive to the soyValue. */
  SoyExpression applyPrintDirective(
      Expression renderContext, String directive, Expression argsList) {
    // Technically the type is either StringData or SanitizedContent depending on this type, but
    // boxed.  Consider propagating the type more accurately, currently there isn't (afaict) much
    // benefit (and strangely there is no common super type for SanitizedContent and String), this
    // is probably because after escaping, the only thing you would ever do is convert to a string.
    return SoyExpression.forSoyValue(
        UnknownType.getInstance(),
        MethodRef.RUNTIME_APPLY_PRINT_DIRECTIVE.invoke(
            renderContext.invoke(MethodRef.RENDER_CONTEXT_GET_PRINT_DIRECTIVE, constant(directive)),
            this.box(),
            argsList));
  }

  @Override
  SoyExpression asCheap() {
    return withSource(delegate.asCheap());
  }

  @Override
  SoyExpression asNonNullable() {
    return new SoyExpression(soyRuntimeType.asNonNullable(), delegate.asNonNullable());
  }

  @Override
  SoyExpression asNullable() {
    return new SoyExpression(soyRuntimeType.asNullable(), delegate.asNullable());
  }

  @Override
  SoyExpression labelStart(Label label) {
    return withSource(delegate.labelStart(label));
  }

  @Override
  SoyExpression labelEnd(Label label) {
    return withSource(delegate.labelEnd(label));
  }
}
