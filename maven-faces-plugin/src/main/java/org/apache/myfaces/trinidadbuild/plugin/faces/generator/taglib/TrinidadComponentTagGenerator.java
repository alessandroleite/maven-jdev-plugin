/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.myfaces.trinidadbuild.plugin.faces.generator.taglib;

import java.io.IOException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.myfaces.trinidadbuild.plugin.faces.generator.GeneratorHelper;
import org.apache.myfaces.trinidadbuild.plugin.faces.io.PrettyWriter;
import org.apache.myfaces.trinidadbuild.plugin.faces.parse.ComponentBean;
import org.apache.myfaces.trinidadbuild.plugin.faces.parse.MethodSignatureBean;
import org.apache.myfaces.trinidadbuild.plugin.faces.parse.PropertyBean;
import org.apache.myfaces.trinidadbuild.plugin.faces.util.FilteredIterator;
import org.apache.myfaces.trinidadbuild.plugin.faces.util.Util;


/**
 * TODO: comment this!
 *
 * @author Bruno Aranda (latest modification by $Author: arobinson74 $)
 * @version $Revision: 1026471 $ $Date: 2010-10-22 16:59:00 -0300 (sex, 22 out 2010) $
 */
public class TrinidadComponentTagGenerator extends AbstractComponentTagGenerator
{
  private boolean _is12;

  public TrinidadComponentTagGenerator(boolean is12)
  {
    _is12 = is12;
  }

  protected boolean is12()
  {
    return _is12;
  }

  protected void addSpecificImports(Set imports,
                                    ComponentBean component)
  {
    imports.add("org.apache.myfaces.trinidad.bean.FacesBean");

    if (_is12)
    {
      imports.add("javax.el.ValueExpression");
    }

    Iterator<PropertyBean> properties = component.properties();
    properties = new FilteredIterator(properties, new TagAttributeFilter());

    while (properties.hasNext())
    {
      PropertyBean property = properties.next();
      String propertyClass = property.getPropertyClass();
      String[] propertyClassParams = property.getPropertyClassParameters();

      imports.add(component.getComponentClass());

      if (GeneratorHelper.isKeyStroke(propertyClass))
      {
        if (_is12)
          imports.add("javax.el.ValueExpression");
        else
          imports.add("javax.faces.el.ValueBinding");
        imports.add("javax.swing.KeyStroke");
      }
      else if (GeneratorHelper.isAWTKeyStroke(propertyClass))
      {
        if (_is12)
          imports.add("javax.el.ValueExpression");
        else
          imports.add("javax.faces.el.ValueBinding");
        imports.add("java.awt.AWTKeyStroke");
      }
      else if (GeneratorHelper.isConverter(propertyClass))
      {
        if (_is12)
          imports.add("javax.el.ValueExpression");
        else
          imports.add("javax.faces.el.ValueBinding");
        imports.add("javax.faces.convert.Converter");
      }
      else if (property.isVirtual())
      {
        if (_is12)
          imports.add("javax.el.ValueExpression");
        else
          imports.add("javax.faces.el.ValueBinding");
        imports.add("org.apache.myfaces.trinidadinternal.taglib.util.VirtualAttributeUtils");
      }
      else if (GeneratorHelper.isColorList(propertyClass, propertyClassParams) ||
               GeneratorHelper.isColor(propertyClass))
      {
        if (_is12)
          imports.add("javax.el.ValueExpression");
        else
          imports.add("javax.faces.el.ValueBinding");
        imports.add("java.text.ParseException");
        imports.add("org.apache.myfaces.trinidadinternal.taglib.util.TagUtils");
      }
      else if (property.isMethodBinding())
      {
        if (_is12)
        {
          imports.add("javax.el.MethodExpression");
          imports.add("org.apache.myfaces.trinidadinternal.taglib.util.MethodExpressionMethodBinding");
        }
        else
        {
          imports.add("javax.faces.el.MethodBinding");
        }
      }
      else if (property.isMethodExpression())
      {
        imports.add("javax.el.MethodExpression");
      }
      else if (property.isEnum())
      {
        imports.add(propertyClass);
      }

      if (property.isNoOp())
      {
        imports.add("org.apache.myfaces.trinidad.logging.TrinidadLogger");
      }
    }
  }


  protected void writePropertyDeclaration(PrettyWriter out,
                                          PropertyBean property) throws IOException
  {
    String propName = property.getPropertyName();
    String fieldPropName = "_" + propName;
    String jspPropType = GeneratorHelper.getJspPropertyType(property, _is12);

    out.println();
    if (property.getDeprecated() != null)
    {
      out.println("@Deprecated");
    }
    out.println("private " + jspPropType + " " + fieldPropName + ";");
  }

  protected void writePropertySetter(PrettyWriter out,
                                     PropertyBean property) throws IOException
  {
    String propName = property.getPropertyName();
    String fieldPropName = "_" + propName;
    String jspPropName = property.getJspPropertyName();
    String propVar = Util.getVariableFromName(propName);
    String setMethod = Util.getPrefixedPropertyName("set", jspPropName);
    String jspPropType = GeneratorHelper.getJspPropertyType(property, _is12);

    if (property.getDeprecated() != null)
    {
      out.println("@Deprecated");
    }
    out.print("final ");
    out.println("public void " + setMethod + "(" + jspPropType + " " + propVar + ")");
    out.println("{");
    out.indent();
    if (property.isNoOp())
    {
      out.println("TrinidadLogger log = TrinidadLogger.createTrinidadLogger(this.getClass());");
      out.print("log.warning(\"property \\\"" + propName + "\\\" setter is ");
      out.print("using a no-op implementation. Used in extreme cases when the property value, beyond the default value, results in unwanted behavior.");
      out.println("\");");
    }
    else
    {
      out.println(fieldPropName + " = " + propVar + ";");
    }
    out.unindent();
    out.println("}");
  }

  public void writeSetPropertiesMethod(PrettyWriter out,
                                       String componentClass,
                                       ComponentBean component) throws IOException
  {
    Collection components = new HashSet();
    components.add(component);
    writeSetPropertiesMethod(out, componentClass, components);
  }


  public void writeSetPropertiesMethod(PrettyWriter out, String componentClass, Collection components)
      throws IOException
  {
    Collection all = new HashSet();
    for (Iterator<ComponentBean> lIterator = components.iterator(); lIterator.hasNext();)
    {
      ComponentBean component = lIterator.next();
      Iterator prop = component.properties();
      while (prop.hasNext())
      {
        all.add(prop.next());
      }
    }

    Iterator properties = all.iterator();
    properties = new FilteredIterator(properties, new TagAttributeFilter());
    properties = new FilteredIterator(properties, new NonOverriddenFilter());

    if (properties.hasNext())
    {
      out.println();
      out.println("@Override");
      out.println("protected void setProperties(");
      out.indent();
      out.println("FacesBean bean)");
      out.unindent();
      out.println("{");
      out.indent();

      writeSetPropertyMethodBody(out, componentClass, properties);
      out.unindent();
      out.println("}");
    }
  }

  protected void writeSetPropertyMethodBody(PrettyWriter out,
                                            String componentClass,
                                            Iterator properties) throws IOException
  {

    out.println("super.setProperties(bean);");

    while (properties.hasNext())
    {
      PropertyBean property = (PropertyBean) properties.next();
      _writeSetPropertiesCase(out, componentClass, property);
    }
  }

  @Override
  protected boolean isSetterMethodFinal()
  {
    return true;
  }

  private void _writeSetPropertiesCase(
      PrettyWriter out,
      String componentClass,
      PropertyBean property) throws IOException
  {
    String propName = property.getPropertyName();
    String propClass = property.getPropertyClass();
    String propVar = "_" + propName;

    if (property.isVirtual())
    {
      _writeVirtualSetMethod(out, componentClass, propName);
    }
    else if (property.isMethodBinding())
    {
      _writeSetMethodBinding(out, componentClass, property);
    }
    else if (property.isMethodExpression())
    {
      _writeSetMethodExpression(out, componentClass, property);
    }
    else if (property.isEnum())
    {
      _writeSetEnum(out, componentClass, property);
    }
    else if (GeneratorHelper.isKeyStroke(propClass))
    {
      _writeSetKeyStroke(out, componentClass, propName);
    }
    else if (GeneratorHelper.isAWTKeyStroke(propClass))
    {
      _writeSetAWTKeyStroke(out, componentClass, propName);
    }
    else if (GeneratorHelper.isColorList(propClass, property.getPropertyClassParameters()))
    {
      _writeSetColor(out, componentClass, propName, true);
    }
    else if (GeneratorHelper.isColor(propClass))
    {
      _writeSetColor(out, componentClass, propName, false);
    }
    else if (GeneratorHelper.isKnownTypeList(propClass,
                                    property.getPropertyClassParameters()))
    {
      _writeSetKnownTypeList (out, componentClass, propName,
                     property.getPropertyClassParameters()[0]);
    }
    else if (GeneratorHelper.isKnownTypeSet(propClass,
                                    property.getPropertyClassParameters()))
    {
      _writeSetKnownTypeSet (out, componentClass, propName,
                     property.getPropertyClassParameters()[0]);
    }
    else if (GeneratorHelper.isConverter(propClass))
    {
      _writeSetConverter(out, componentClass, propName);
    }
    else if (property.isLiteralOnly())
    {
      _writeSetLiteral(out, componentClass, propName, propClass, propVar);
    }
    else if ("java.util.Date".equals(propClass))
    {
      _writeSetDate(out, componentClass, propName, propClass, propVar, property.getUseMaxTime());
    }
    else //if (_hasPropertySetter(property))
    {
      _writeSetProperty(out, componentClass, propName, propClass, propVar);
    }
    //    else
    //    {
    //      _writeSetValueBinding(out, componentClass, propName, propVar);
    //    }
  }

  private void _writeSetLiteral(
      PrettyWriter out,
      String componentClass,
      String propName,
      String propFullClass,
      String propVar)
  {
    String propClass = Util.getClassFromFullClass(propFullClass);
    String boxedClass = Util.getBoxedClass(propClass);
    if ((!_is12 && !boxedClass.equals(propClass)) ||
        "java.util.Date".equals(propFullClass) ||
        (boxedClass.indexOf("[]") != -1))
    {
      // TODO: reject value binding expressions for literal-only
      _writeSetProperty(out, componentClass, propName, propFullClass, propVar);
    }
    else
    {
      String propKey = Util.getConstantNameFromProperty(propName, "_KEY");
      out.println("bean.setProperty(" +
          componentClass + "." + propKey + ", " +
          propVar + ");");
    }
  }

  private void _writeSetDate(
    PrettyWriter out,
    String componentClass,
    String propName,
    String propFullClass,
    String propVar,
    boolean useMaxTime)
  {
    String propKey = Util.getConstantNameFromProperty(propName, "_KEY");
    String propClass = Util.getClassFromFullClass(propFullClass);
    String boxedClass = Util.getBoxedClass(propClass);
    String setProperty = "setProperty";
    String propType = boxedClass.replaceAll("\\[\\]", "Array");
    if (useMaxTime)
    {
      setProperty = Util.getPrefixedPropertyName("setMax", propType + "Property");
    }
    else
    {
      setProperty = Util.getPrefixedPropertyName("set", propType + "Property");
    }

    out.println(setProperty + "(bean, " +
        componentClass + "." + propKey + ", " +
        propVar + ");");
  }

  private void _writeSetProperty(
      PrettyWriter out,
      String componentClass,
      String propName,
      String propFullClass,
      String propVar)
  {
    String propKey = Util.getConstantNameFromProperty(propName, "_KEY");
    String propClass = Util.getClassFromFullClass(propFullClass);
    String boxedClass = Util.getBoxedClass(propClass);
    String setProperty = "setProperty";
    if ((!_is12 && !boxedClass.equals(propClass)) ||
        GeneratorHelper.isNumber(propFullClass) ||
        "java.util.Date".equals(propFullClass) ||
        (boxedClass.indexOf("[]") != -1))
    {
      String propType = boxedClass.replaceAll("\\[\\]", "Array");
      setProperty = Util.getPrefixedPropertyName("set", propType + "Property");
    }

    out.println(setProperty + "(bean, " +
        componentClass + "." + propKey + ", " +
        propVar + ");");
  }

  private String[] _getAccessKeyPropertyKeys(
      String componentClass,
      String propName)
  {
    String[] propKeys = new String[2];

    int offset = propName.indexOf("AndAccessKey");
    if (offset != -1)
    {
      String mainProp = propName.substring(0, offset);
      propKeys[0] = componentClass + "." +
          Util.getConstantNameFromProperty(mainProp, "_KEY");
      propKeys[1] = componentClass + "." +
          Util.getConstantNameFromProperty("accessKey", "_KEY");
    }

    return propKeys;
  }

  private void _writeSetValueBinding(
      PrettyWriter out,
      String componentClass,
      String propName,
      String propVar)
  {
    String propKey = Util.getConstantNameFromProperty(propName, "_KEY");
    out.println("bean.setValueBinding(" + componentClass + "." + propKey + ", " +
        "createValueBinding(" + propVar + "));");
  }

  private void _writeVirtualSetMethod(
      PrettyWriter out,
      String componentClass,
      String propName) throws IOException
  {
    String[] propKeys = _getAccessKeyPropertyKeys(componentClass, propName);

    String propVar = "_" + propName;
    out.println("if (" + propVar + " != null)");
    out.println("{");
    out.indent();

    if (_is12)
    {
      out.println("if (!" + propVar + ".isLiteralText())");
      out.println("{");
      out.indent();
      out.println("VirtualAttributeUtils.setAccessKeyAttribute(");
      out.indent();
      out.println("bean,");
      out.println(propVar + ",");
      out.println(propKeys[0] + ",");
      out.println(propKeys[1] + ");");
      out.unindent();
      out.unindent();
      out.println("}");
      out.println("else");
      out.println("{");
      out.indent();
      out.println("String s = " + propVar + ".getExpressionString();");
      out.println("if (s != null)");
      out.indent();
      out.println("VirtualAttributeUtils.setAccessKeyAttribute(");
      out.indent();
      out.println("bean,");
      out.println("s,");
      out.println(propKeys[0] + ",");
      out.println(propKeys[1] + ");");
      out.unindent();
      out.unindent();
      out.unindent();
      out.println("}");
    }
    else
    {
      out.println("if (isValueReference(" + propVar + "))");
      out.println("{");
      out.indent();
      out.println("ValueBinding vb = createValueBinding(" + propVar + ");");
      out.println("VirtualAttributeUtils.setAccessKeyAttribute(");
      out.indent();
      out.println("bean,");
      out.println("vb,");
      out.println(propKeys[0] + ",");
      out.println(propKeys[1] + ");");
      out.unindent();
      out.unindent();
      out.println("}");
      out.println("else");
      out.println("{");
      out.indent();
      out.println("VirtualAttributeUtils.setAccessKeyAttribute(");
      out.indent();
      out.println("bean,");
      out.println(propVar + ",");
      out.println(propKeys[0] + ",");
      out.println(propKeys[1] + ");");
      out.unindent();
      out.unindent();
      out.println("}");
    }

    out.unindent();
    out.println("}");
  }

  private void _writeSetMethodBinding(
      PrettyWriter out,
      String componentClass,
      PropertyBean property) throws IOException
  {
    String propName = property.getPropertyName();
    String propKey = Util.getConstantNameFromProperty(propName, "_KEY");
    String propVar = "_" + propName;

    if (_is12)
    {
      out.println("if (" + propVar + " != null)");
      out.indent();
      out.println("bean.setProperty(" + componentClass + "." + propKey + ", " +
          "new MethodExpressionMethodBinding(" + propVar + "));");
      out.unindent();
    }
    else
    {
      MethodSignatureBean signature = property.getMethodBindingSignature();
      String[] paramTypes = (signature != null) ? signature.getParameterTypes() : null;

      String classArray;

      if (paramTypes == null || paramTypes.length == 0)
      {
        classArray = "new Class[0]";
      }
      else
      {
        StringBuffer sb = new StringBuffer();
        sb.append("new Class[]{");
        for (int i = 0; i < paramTypes.length; i++)
        {
          if (i > 0)
            sb.append(',');
          sb.append(paramTypes[i]);
          sb.append(".class");
        }

        // TODO: remove trailing comma
        sb.append(',');

        sb.append('}');
        classArray = sb.toString();
      }

      out.println("if (" + propVar + " != null)");
      out.println("{");
      out.indent();

      if (isStringMethodBindingReturnType(signature))
      {
        out.println("MethodBinding mb;");
        out.println("if (isValueReference(" + propVar + "))");
        out.indent();
        out.println("mb = createMethodBinding(" + propVar + ", " + classArray + ");");
        out.unindent();
        out.println("else");
        out.indent();
        out.println("mb = new org.apache.myfaces.trinidadinternal.taglib.ConstantMethodBinding(" + propVar + ");");
        out.unindent();
      }
      else
      {
        // never a literal, no need for ConstantMethodBinding
        out.println("MethodBinding mb = createMethodBinding(" + propVar + ", " +
            classArray + ");");
      }

      out.println("bean.setProperty(" + componentClass + "." + propKey + ", mb);");
      out.unindent();
      out.println("}");
    }
  }

  private void _writeSetMethodExpression(
      PrettyWriter out,
      String componentClass,
      PropertyBean property) throws IOException
  {
    String propName = property.getPropertyName();
    String propKey = Util.getConstantNameFromProperty(propName, "_KEY");
    String propVar = "_" + propName;

    out.println("bean.setProperty(" + componentClass + "." + propKey + ", " +
        propVar + ");");
  }

  private void _writeSetEnum(
      PrettyWriter out,
      String componentClass,
      PropertyBean property) throws IOException
  {
    String propName = property.getPropertyName();
    String propKey = Util.getConstantNameFromProperty(propName, "_KEY");
    String propVar = "_" + propName;
    String propClass = Util.getClassFromFullClass(property.getPropertyClass());

    if (_is12)
    {
      out.println("if (" + propVar + " != null)");
      out.println("{");
      out.indent();
      out.println("if (!" + propVar + ".isLiteralText())");
      out.println("{");
      out.indent();
      out.println("bean.setValueExpression(" + componentClass + "." + propKey + ", " + propVar + ");");
      out.unindent();
      out.println("}");
      out.println("else");
      out.println("{");
      out.indent();
      out.println("String s = " + propVar + ".getExpressionString();");
      out.println("if (s != null)");
      out.indent();
      out.println("bean.setProperty(" + componentClass + "." + propKey + ",");
      out.println("Enum.valueOf(" + propClass + ".class,s));");
      out.unindent();
      out.unindent();
      out.println("}");
      out.unindent();
      out.println("}");
    }
    else
    {
      out.println("if (" + propVar + " != null)");
      out.println("{");
      out.indent();
      out.println("if (isValueReference(" + propVar + "))");
      out.println("{");
      out.indent();
      out.println("ValueBinding vb = createValueBinding(" + propVar + ");");
      out.println("bean.setValueBinding(" + componentClass + "." + propKey + ", vb);");
      out.unindent();
      out.println("}");
      out.println("else");
      out.println("{");
      out.indent();
      out.println("bean.setProperty(" + componentClass + "." + propKey + ",");
      out.println("Enum.valueOf(" + propClass + ".class," + propVar + "));");
      out.unindent();
      out.println("}");
      out.unindent();
      out.println("}");
    }
  }


  private void _writeSetKeyStroke(
      PrettyWriter out,
      String componentClass,
      String propName) throws IOException
  {
    String propKey = Util.getConstantNameFromProperty(propName, "_KEY");
    String propVar = "_" + propName;


    if (_is12)
    {
      out.println("if (" + propVar + " != null)");
      out.println("{");
      out.indent();
      out.println("if (!" + propVar + ".isLiteralText())");
      out.println("{");
      out.indent();
      out.println("bean.setValueExpression(" + componentClass + "." + propKey + ", " + propVar + ");");
      out.unindent();
      out.println("}");
      out.println("else");
      out.println("{");
      out.indent();
      out.println("String val = " + propVar + ".getExpressionString();");
      out.println("if (val != null)");
      out.indent();
      out.println("bean.setProperty(" + componentClass + "." + propKey + ",");
      out.println("\tKeyStroke.getKeyStroke(val));");
      out.unindent();
      out.unindent();
      out.println("}");
      out.unindent();
      out.println("}");
    }
    else
    {
      out.println("if (" + propVar + " != null)");
      out.println("{");
      out.indent();
      out.println("if (isValueReference(" + propVar + "))");
      out.println("{");
      out.indent();
      out.println("ValueBinding vb = createValueBinding(" + propVar + ");");
      out.println("bean.setValueBinding(" + componentClass + "." + propKey + ", vb);");
      out.unindent();
      out.println("}");
      out.println("else");
      out.println("{");
      out.indent();
      out.println("bean.setProperty(" + componentClass + "." + propKey + ",");
      out.println("\tKeyStroke.getKeyStroke(" + propVar + "));");
      out.unindent();
      out.println("}");
      out.unindent();
      out.println("}");
    }
  }


  private void _writeSetAWTKeyStroke(
      PrettyWriter out,
      String componentClass,
      String propName) throws IOException
  {
    String propKey = Util.getConstantNameFromProperty(propName, "_KEY");
    String propVar = "_" + propName;

    if (_is12)
    {
      out.println("if (" + propVar + " != null)");
      out.println("{");
      out.indent();
      out.println("if (!" + propVar + ".isLiteralText())");
      out.println("{");
      out.indent();
      out.println("bean.setValueExpression(" + componentClass + "." + propKey + ", " + propVar + ");");
      out.unindent();
      out.println("}");
      out.println("else");
      out.println("{");
      out.indent();
      out.println("String val = " + propVar + ".getExpressionString();");
      out.println("if (val != null)");
      out.indent();
      out.println("bean.setProperty(" + componentClass + "." + propKey + ",");
      out.println("\tAWTKeyStroke.getAWTKeyStroke(val));");
      out.unindent();
      out.unindent();
      out.println("}");
      out.unindent();
      out.println("}");
    }
    else
    {
      out.println("if (" + propVar + " != null)");
      out.println("{");
      out.indent();
      out.println("if (isValueReference(" + propVar + "))");
      out.println("{");
      out.indent();
      out.println("ValueBinding vb = createValueBinding(" + propVar + ");");
      out.println("bean.setValueBinding(" + componentClass + "." + propKey + ", vb);");
      out.unindent();
      out.println("}");
      out.println("else");
      out.println("{");
      out.indent();
      out.println("bean.setProperty(" + componentClass + "." + propKey + ",");
      out.println("\tAWTKeyStroke.getAWTKeyStroke(" + propVar + "));");
      out.unindent();
      out.println("}");
      out.unindent();
      out.println("}");
    }
  }


  private void _writeSetColor(
      PrettyWriter out,
      String  componentClass,
      String  propName,
      boolean isList) throws IOException
  {
    String propKey = Util.getConstantNameFromProperty(propName, "_KEY");
    String propVar = "_" + propName;

    out.println("if (" + propVar + " != null)");
    out.println("{");
    out.indent();
    if (_is12)
    {
      out.println("if (!" + propVar + ".isLiteralText())");
      out.println("{");
      out.indent();
      out.println("bean.setValueExpression(" + componentClass + "." + propKey + ", " + propVar + ");");
      out.unindent();
      out.println("}");
      out.println("else");
      out.println("{");
      out.indent();
      out.println("String s = " + propVar + ".getExpressionString();");
      out.println("if (s != null)");
      out.indent();
      out.println("{");
      out.println("try");
      out.println("{");
      out.indent();
      out.println("bean.setProperty(" + componentClass + "." + propKey + ",");
      if (isList)
        out.println("                 TagUtils.getColorList(s));");
      else
      {
        out.println("                 TagUtils.getColor(s));");
      }
      out.unindent();
      out.println("}");
      out.println("catch (ParseException pe)");
      out.println("{");
      out.indent();
      out.println("setValidationError(");
      out.println("  pe.getMessage() + \": \" + \"Position \" + pe.getErrorOffset());");
      out.unindent();
      out.println("}");
      out.unindent();
      out.println("}");
      out.unindent();
      out.println("}");
    }
    else
    {
      out.println("if (isValueReference(" + propVar + "))");
      out.println("{");
      out.indent();
      out.println("ValueBinding vb = createValueBinding(" + propVar + ");");
      out.println("bean.setValueBinding(" + componentClass + "." + propKey + ", vb);");
      out.unindent();
      out.println("}");
      out.println("else");
      out.println("{");
      out.indent();
      out.println("try");
      out.println("{");
      out.indent();
      out.println("bean.setProperty(" + componentClass + "." + propKey + ",");
      if (isList)
	    out.println("                 TagUtils.getColorList(" + propVar + "));");
	  else
        out.println("                 TagUtils.getColor(" + propVar + "));");
      out.unindent();
      out.println("}");
      out.println("catch (ParseException pe)");
      out.println("{");
      out.indent();
      out.println("setValidationError(");
      out.println("  pe.getMessage() + \": \" + \"Position \" + pe.getErrorOffset());");
      out.unindent();
      out.println("}");
      out.unindent();
      out.println("}");
    }
    out.unindent();
    out.println("}");
  }

  private void _writeSetKnownTypeList(
      PrettyWriter out,
      String  componentClass,
      String  propName,
      String  propFullClass) throws IOException
  {
    String propKey = Util.getConstantNameFromProperty(propName, "_KEY");
    String propVar = "_" + propName;

    String propClass = Util.getClassFromFullClass(propFullClass);
    String boxedClass = Util.getBoxedClass(propClass);

    System.out.println ("_writeSetList: propFullClass = " + propFullClass +
                        " propClass= " + propClass +
                        " boxedClass=" + boxedClass);
    if (_is12)
    {
      out.println("set" + boxedClass + "ListProperty" +
                  "(bean, " + componentClass + "." + propKey +
                  ", " + propVar + ");");
    }
    else
    {
      out.println("if (" + propVar + " != null)");
      out.println("{");
      out.indent();
      out.println("if (isValueReference(" + propVar + "))");
      out.println("{");
      out.indent();
      out.println("ValueBinding vb = createValueBinding(" + propVar + ");");
      out.println("bean.setValueBinding(" + componentClass + "." + propKey + ", vb);");
      out.unindent();
      out.println("}");
      out.println("else");
      out.println("{");
      out.indent();
      out.println("try");
      out.println("{");
      out.indent();
      out.println("bean.setProperty(" + componentClass + "." + propKey + ",");
      out.println("                 TagUtils.getStringList(" + propVar + "));");
      out.unindent();
      out.println("}");
      out.println("catch (ParseException pe)");
      out.println("{");
      out.indent();
      out.println("setValidationError(");
      out.println("  pe.getMessage() + \": \" + \"Position \" + pe.getErrorOffset());");
      out.unindent();
      out.println("}");
      out.unindent();
      out.println("}");
      out.unindent();
      out.println("}");
    }
  }

  private void _writeSetKnownTypeSet(
      PrettyWriter out,
      String  componentClass,
      String  propName,
      String  propFullClass) throws IOException
  {
    String propKey = Util.getConstantNameFromProperty(propName, "_KEY");
    String propVar = "_" + propName;

    String propClass = Util.getClassFromFullClass(propFullClass);
    String boxedClass = Util.getBoxedClass(propClass);

    System.out.println ("_writeSetSet: propFullClass = " + propFullClass +
                        " propClass= " + propClass +
                        " boxedClass=" + boxedClass);
    if (_is12)
    {
      out.println("set" + boxedClass + "SetProperty" +
                  "(bean, " + componentClass + "." + propKey +
                  ", " + propVar + ");");
    }
    else
    {
      out.println("if (" + propVar + " != null)");
      out.println("{");
      out.indent();
      out.println("if (isValueReference(" + propVar + "))");
      out.println("{");
      out.indent();
      out.println("ValueBinding vb = createValueBinding(" + propVar + ");");
      out.println("bean.setValueBinding(" + componentClass + "." + propKey + ", vb);");
      out.unindent();
      out.println("}");
      out.println("else");
      out.println("{");
      out.indent();
      out.println("try");
      out.println("{");
      out.indent();
      out.println("bean.setProperty(" + componentClass + "." + propKey + ",");
      out.println("                 TagUtils.getStringSet(" + propVar + "));");
      out.unindent();
      out.println("}");
      out.println("catch (ParseException pe)");
      out.println("{");
      out.indent();
      out.println("setValidationError(");
      out.println("  pe.getMessage() + \": \" + \"Position \" + pe.getErrorOffset());");
      out.unindent();
      out.println("}");
      out.unindent();
      out.println("}");
      out.unindent();
      out.println("}");
    }
  }

  private void _writeSetConverter(
      PrettyWriter out,
      String componentClass,
      String propName) throws IOException
  {
    String propKey = Util.getConstantNameFromProperty(propName, "_KEY");
    String propVar = "_" + propName;

    out.println("if (" + propVar + " != null)");
    out.println("{");
    out.indent();
    if (_is12)
    {
      out.println("if (!" + propVar + ".isLiteralText())");
      out.println("{");
      out.indent();
      out.println("bean.setValueExpression(" + componentClass + "." + propKey + ", " + propVar + ");");
      out.unindent();
      out.println("}");
      out.println("else");
      out.println("{");
      out.indent();
      out.println("String s = " + propVar + ".getExpressionString();");
      out.println("if (s != null)");
      out.println("{");

      out.indent();
      out.println("Converter converter = getFacesContext().getApplication().");
      out.indent();
      out.println("createConverter(s);");
      out.unindent();
      out.println("bean.setProperty(" + componentClass + "." + propKey + ", converter);");

      out.unindent();
      out.println("}");

      out.unindent();
      out.println("}");
    }
    else
    {
      out.println("if (isValueReference(" + propVar + "))");
      out.println("{");
      out.indent();
      out.println("ValueBinding vb = createValueBinding(" + propVar + ");");
      out.println("bean.setValueBinding(" + componentClass + "." + propKey + ", vb);");
      out.unindent();
      out.println("}");
      out.println("else");
      out.println("{");
      out.indent();
      out.println("Converter converter = getFacesContext().getApplication().");
      out.indent();
      out.println("createConverter(" + propVar + ");");
      out.unindent();
      out.println("bean.setProperty(" + componentClass + "." + propKey + ", converter);");
      out.unindent();
      out.println("}");
    }
    out.unindent();
    out.println("}");
  }

  private boolean isStringMethodBindingReturnType(
      MethodSignatureBean sig)
  {
    return (sig != null && "java.lang.String".equals(sig.getReturnType()));
  }

}
