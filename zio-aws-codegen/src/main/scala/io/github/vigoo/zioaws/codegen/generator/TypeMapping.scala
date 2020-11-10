package io.github.vigoo.zioaws.codegen.generator

import software.amazon.awssdk.codegen.model.service.Shape
import zio.ZIO

import scala.meta._
import _root_.io.github.vigoo.zioaws.codegen.generator.context._

object TypeMapping {
  def isPrimitiveType(shape: Shape): Boolean =
    shape.getType match {
      case "string" if Option(shape.getEnumValues).isEmpty => true
      case "integer"                                       => true
      case "long"                                          => true
      case "float"                                         => true
      case "double"                                        => true
      case "boolean"                                       => true
      case "timestamp"                                     => true
      case "blob"                                          => true
      case "bigdecimal"                                    => true
      case "map" => true
      case "list" => true
      case _                                               => false
    }

  private val builtIns = Set(
    "String",
    "Boolean",
    "Int",
    "Integer",
    "Long",
    "Float",
    "Double",
    "BigDecimal"
  )

  def isBuiltIn(name: String): Boolean = {
    builtIns.contains(name)
  }

  def toJavaType(
      model: Model
  ): ZIO[GeneratorContext, GeneratorFailure, Type] = {
    getModelPkg.flatMap { modelPkg =>
      val shape = model.shape
      model.typ match {
        case ModelType.Map =>
          for {
            keyModel <- get(shape.getMapKeyType.getShape)
            keyType <- toJavaType(keyModel)
            valueModel <- get(shape.getMapValueType.getShape)
            valueType <- toJavaType(valueModel)
          } yield t"""java.util.Map[$keyType, $valueType]"""
        case ModelType.List =>
          for {
            itemModel <- get(shape.getListMember.getShape)
            itemType <- toJavaType(itemModel)
          } yield t"""java.util.List[$itemType]"""
        case ModelType.Enum =>
          ZIO.succeed(Type.Select(modelPkg, Type.Name(model.name)))
        case ModelType.String =>
          ZIO.succeed(t"""java.lang.String""")
        case ModelType.Integer =>
          ZIO.succeed(t"""java.lang.Integer""")
        case ModelType.Long =>
          ZIO.succeed(t"""java.lang.Long""")
        case ModelType.Float =>
          ZIO.succeed(t"""java.lang.Float""")
        case ModelType.Double =>
          ZIO.succeed(t"""java.lang.Double""")
        case ModelType.Boolean =>
          ZIO.succeed(t"""java.lang.Boolean""")
        case ModelType.Timestamp =>
          ZIO.succeed(t"""java.time.Instant""")
        case ModelType.BigDecimal =>
          ZIO.succeed(t"""java.math.BigDecimal""")
        case ModelType.Blob =>
          ZIO.succeed(t"""SdkBytes""")
        case ModelType.Exception =>
          ZIO.succeed(Type.Select(modelPkg, Type.Name(model.name)))
        case ModelType.Structure =>
          ZIO.succeed(Type.Select(modelPkg, Type.Name(model.name)))
        case ModelType.Unknown(typ) =>
          getServiceName.flatMap(svc => ZIO.fail(UnknownType(svc, typ)))
      }
    }
  }

  def toWrappedType(
      model: Model
  ): ZIO[GeneratorContext, GeneratorFailure, Type] = {
    model.typ match {
      case ModelType.Map =>
        for {
          keyModel <- get(model.shape.getMapKeyType.getShape)
          keyType <- toWrappedType(keyModel)
          valueModel <- get(model.shape.getMapValueType.getShape)
          valueType <- toWrappedType(valueModel)
        } yield t"""Map[$keyType, $valueType]"""
      case ModelType.List =>
        for {
          itemModel <- get(model.shape.getListMember.getShape)
          itemType <- toWrappedType(itemModel)
        } yield t"""Iterable[$itemType]"""
      case _ if isPrimitiveType(model.shape) && !isBuiltIn(model.shapeName) =>
        ZIO.succeed(Type.Select(Term.Name("primitives"), Type.Name(model.name)))
      case _ =>
        ZIO.succeed(Type.Name(model.name))
    }
  }

  def toWrappedTypeReadOnly(
      model: Model
  ): ZIO[GeneratorContext, GeneratorFailure, Type] = {
    model.typ match {
      case ModelType.Map =>
        for {
          keyModel <- get(model.shape.getMapKeyType.getShape)
          keyType <- toWrappedTypeReadOnly(keyModel)
          valueModel <- get(model.shape.getMapValueType.getShape)
          valueType <- toWrappedTypeReadOnly(valueModel)
        } yield t"""Map[$keyType, $valueType]"""
      case ModelType.List =>
        for {
          itemModel <- get(model.shape.getListMember.getShape)
          itemType <- toWrappedTypeReadOnly(itemModel)
        } yield t"""List[$itemType]"""
      case ModelType.Exception =>
        toJavaType(model)
      case ModelType.Structure =>
        ZIO.succeed(Type.Select(Term.Name(model.name), Type.Name("ReadOnly")))
      case _ if isPrimitiveType(model.shape) && !isBuiltIn(model.shapeName) =>
        ZIO.succeed(Type.Select(Term.Name("primitives"), Type.Name(model.name)))
      case _ =>
        ZIO.succeed(Type.Name(model.name))
    }
  }
}
