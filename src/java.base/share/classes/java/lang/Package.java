/*
 * Copyright (c) 1997, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.net.URL;
import java.util.Objects;

import jdk.internal.misc.BootLoader;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;


/**
 * Represents metadata about a run-time package associated with a class loader.
 * Metadata includes annotations, versioning, and sealing.
 * <p>
 * Annotations for the run-time package are read from {@code package-info.class}
 * at the same code source as classes in the run-time package.
 * <p>
 * The set of classes that make up the run-time package may implement a
 * particular specification. The specification title, version, and vendor
 * (indicating the owner/maintainer of the specification) can be provided
 * when the {@code Package} is defined. An application can ask if the
 * {@code Package} is compatible with a particular specification version
 * by using the {@link #isCompatibleWith Package.isCompatibleWith(String)}
 * method. In addition, information about the actual classes that make up the
 * run-time package can be provided when the Package is defined.
 * This information consists of an implementation title, version, and vendor
 * (indicating the supplier of the classes).
 * <p>
 * A {@code Package} may be explicitly defined with
 * the {@link ClassLoader#definePackage(String, String, String, String,
 * String, String, String, URL)} method.
 * The caller supplies the specification and implementation titles, versions, and
 * vendors. The caller also indicates whether the package is
 * {@linkplain java.util.jar.Attributes.Name#SEALED sealed}.
 * If a {@code Package} is not explicitly defined for a run-time package when
 * a class in that run-time package is defined, then a {@code Package} is
 * automatically defined by the class's defining class loader, as follows.
 * <p>
 * A {@code Package} automatically defined for classes in a named module has
 * the following properties:
 * <ul>
 * <li>The name of the package is derived from the {@linkplain Class#getName() binary names}
 *     of the classes. Since classes in a named module must be in a named package,
 *     the derived name is never empty.</li>
 * <li>The package is sealed with the {@linkplain java.lang.module.ModuleReference#location()
 *     module location} as the code source, if known.</li>
 * <li>The specification and implementation titles, versions, and vendors
 *     are unspecified.</li>
 * <li>Any annotations on the package are read from {@code package-info.class}
 *     as specified above.</li>
 * </ul>
 * <p>
 * A {@code Package} automatically defined for classes in an unnamed module
 * has the following properties:
 * <ul>
 * <li>The name of the package is either {@code ""} (for classes in an unnamed package)
 *     or derived from the {@linkplain Class#getName() binary names} of the classes
 *     (for classes in a named package).</li>
 * <li>The package is not sealed.</li>
 * <li>The specification and implementation titles, versions, and vendors
 *     are unspecified.</li>
 * <li>Any annotations on the package are read from {@code package-info.class}
 *     as specified above.</li>
 * </ul>
 *
 * <p>
 * A {@code Package} can be obtained with the {@link Package#getPackage
 * Package.getPackage(String)} and {@link ClassLoader#getDefinedPackage
 * ClassLoader.getDefinedPackage(String)} methods.
 * Every {@code Package} defined by a class loader can be obtained
 * with the {@link Package#getPackages Package.getPackages()} and
 * {@link ClassLoader#getDefinedPackages} methods.
 *
 * @jvms 5.3 Run-time package
 * @see <a href="../../../technotes/guides/jar/jar.html#versioning">
 * The JAR File Specification: Package Versioning</a>
 * @see <a href="../../../technotes/guides/jar/jar.html#sealing">
 * The JAR File Specification: Package Sealing</a>
 * @see ClassLoader#definePackage(String, String, String, String, String, String, String, URL)
 *
 * @since 1.2
 */
public class Package implements java.lang.reflect.AnnotatedElement {
    /**
     * Return the name of this package.
     *
     * @return  The fully-qualified name of this package as defined in section 6.5.3 of
     *          <cite>The Java&trade; Language Specification</cite>,
     *          for example, {@code java.lang}
     */
    public String getName() {
        return pkgName;
    }


    /**
     * Return the title of the specification that this package implements.
     * @return the specification title, {@code null} is returned if it is not known.
     */
    public String getSpecificationTitle() {
        return specTitle;
    }

    /**
     * Returns the version number of the specification
     * that this package implements.
     * This version string must be a sequence of non-negative decimal
     * integers separated by "."'s and may have leading zeros.
     * When version strings are compared the most significant
     * numbers are compared.
     *
     *
     * <p>Specification version numbers use a syntax that consists of non-negative
     * decimal integers separated by periods ".", for example "2.0" or
     * "1.2.3.4.5.6.7".  This allows an extensible number to be used to represent
     * major, minor, micro, etc. versions.  The version specification is described
     * by the following formal grammar:
     * <blockquote>
     * <dl>
     * <dt><i>SpecificationVersion:</i>
     * <dd><i>Digits RefinedVersion<sub>opt</sub></i>

     * <dt><i>RefinedVersion:</i>
     * <dd>{@code .} <i>Digits</i>
     * <dd>{@code .} <i>Digits RefinedVersion</i>
     *
     * <dt><i>Digits:</i>
     * <dd><i>Digit</i>
     * <dd><i>Digits</i>
     *
     * <dt><i>Digit:</i>
     * <dd>any character for which {@link Character#isDigit} returns {@code true},
     * e.g. 0, 1, 2, ...
     * </dl>
     * </blockquote>
     *
     * @return the specification version, {@code null} is returned if it is not known.
     */
    public String getSpecificationVersion() {
        return specVersion;
    }

    /**
     * Return the name of the organization, vendor,
     * or company that owns and maintains the specification
     * of the classes that implement this package.
     * @return the specification vendor, {@code null} is returned if it is not known.
     */
    public String getSpecificationVendor() {
        return specVendor;
    }

    /**
     * Return the title of this package.
     * @return the title of the implementation, {@code null} is returned if it is not known.
     */
    public String getImplementationTitle() {
        return implTitle;
    }

    /**
     * Return the version of this implementation. It consists of any string
     * assigned by the vendor of this implementation and does
     * not have any particular syntax specified or expected by the Java
     * runtime. It may be compared for equality with other
     * package version strings used for this implementation
     * by this vendor for this package.
     * @return the version of the implementation, {@code null} is returned if it is not known.
     */
    public String getImplementationVersion() {
        return implVersion;
    }

    /**
     * Returns the vendor that implemented this package, {@code null}
     * is returned if it is not known.
     * @return the vendor that implemented this package, {@code null}
     * is returned if it is not known.
     */
    public String getImplementationVendor() {
        return implVendor;
    }

    /**
     * Returns true if this package is sealed.
     *
     * @return true if the package is sealed, false otherwise
     */
    public boolean isSealed() {
        return sealBase != null;
    }

    /**
     * Returns true if this package is sealed with respect to the specified
     * code source {@code url}.
     *
     * @param url the code source URL
     * @return true if this package is sealed with respect to the given {@code url}
     */
    public boolean isSealed(URL url) {
        return url.equals(sealBase);
    }

    /**
     * Compare this package's specification version with a
     * desired version. It returns true if
     * this packages specification version number is greater than or equal
     * to the desired version number. <p>
     *
     * Version numbers are compared by sequentially comparing corresponding
     * components of the desired and specification strings.
     * Each component is converted as a decimal integer and the values
     * compared.
     * If the specification value is greater than the desired
     * value true is returned. If the value is less false is returned.
     * If the values are equal the period is skipped and the next pair of
     * components is compared.
     *
     * @param desired the version string of the desired version.
     * @return true if this package's version number is greater
     *          than or equal to the desired version number
     *
     * @exception NumberFormatException if the current version is not known or
     *          the desired or current version is not of the correct dotted form.
     */
    public boolean isCompatibleWith(String desired)
        throws NumberFormatException
    {
        if (specVersion == null || specVersion.length() < 1) {
            throw new NumberFormatException("Empty version string");
        }

        String [] sa = specVersion.split("\\.", -1);
        int [] si = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            si[i] = Integer.parseInt(sa[i]);
            if (si[i] < 0)
                throw NumberFormatException.forInputString("" + si[i]);
        }

        String [] da = desired.split("\\.", -1);
        int [] di = new int[da.length];
        for (int i = 0; i < da.length; i++) {
            di[i] = Integer.parseInt(da[i]);
            if (di[i] < 0)
                throw NumberFormatException.forInputString("" + di[i]);
        }

        int len = Math.max(di.length, si.length);
        for (int i = 0; i < len; i++) {
            int d = (i < di.length ? di[i] : 0);
            int s = (i < si.length ? si[i] : 0);
            if (s < d)
                return false;
            if (s > d)
                return true;
        }
        return true;
    }

    /**
     * Finds a package by name in the caller's class loader and its
     * ancestors.
     * <p>
     * If the caller's class loader defines a {@code Package} of the given name,
     * the {@code Package} is returned. Otherwise, the ancestors of the
     * caller's class loader are searched recursively (parent by parent)
     * for a {@code Package} of the given name.
     * <p>
     * Calling this method is equivalent to calling {@link ClassLoader#getPackage}
     * on a {@code ClassLoader} instance which is the caller's class loader.
     *
     * @param name A package name, such as "{@code java.lang}".
     * @return The {@code Package} of the given name defined by the caller's
     *         class loader or its ancestors, or {@code null} if not found.
     *
     * @see ClassLoader#getDefinedPackage
     */
    @CallerSensitive
    @SuppressWarnings("deprecation")
    public static Package getPackage(String name) {
        ClassLoader l = ClassLoader.getClassLoader(Reflection.getCallerClass());
        return l != null ? l.getPackage(name) : BootLoader.getPackage(name);
    }

    /**
     * Returns all of the {@code Package}s defined by the caller's class loader
     * and its ancestors.  The returned array may contain more than one
     * {@code Package} object of the same package name, each defined by
     * a different class loader in the class loader hierarchy.
     * <p>
     * Calling this method is equivalent to calling {@link ClassLoader#getPackages}
     * on a {@code ClassLoader} instance which is the caller's class loader.
     *
     * @return  The array of {@code Package} objects defined by this
     *          class loader and its ancestors
     *
     * @see ClassLoader#getDefinedPackages
     */
    @CallerSensitive
    public static Package[] getPackages() {
        ClassLoader cl = ClassLoader.getClassLoader(Reflection.getCallerClass());
        return cl != null ? cl.getPackages() : BootLoader.packages().toArray(Package[]::new);
    }

    /**
     * Return the hash code computed from the package name.
     * @return the hash code computed from the package name.
     */
    public int hashCode(){
        return pkgName.hashCode();
    }

    /**
     * Returns the string representation of this Package.
     * Its value is the string "package " and the package name.
     * If the package title is defined it is appended.
     * If the package version is defined it is appended.
     * @return the string representation of the package.
     */
    public String toString() {
        String spec = specTitle;
        String ver =  specVersion;
        if (spec != null && spec.length() > 0)
            spec = ", " + spec;
        else
            spec = "";
        if (ver != null && ver.length() > 0)
            ver = ", version " + ver;
        else
            ver = "";
        return "package " + pkgName + spec + ver;
    }

    private Class<?> getPackageInfo() {
        if (packageInfo == null) {
            // find package-info.class defined by loader
            String cn = pkgName + ".package-info";
            Class<?> c = loader != null ? loader.loadLocalClassOrNull(cn)
                                        : BootLoader.loadClassOrNull(cn);
            if (c != null) {
                packageInfo = c;
            } else {
                // store a proxy for the package info that has no annotations
                class PackageInfoProxy {}
                packageInfo = PackageInfoProxy.class;
            }
        }
        return packageInfo;
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return getPackageInfo().getAnnotation(annotationClass);
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return AnnotatedElement.super.isAnnotationPresent(annotationClass);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public  <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
        return getPackageInfo().getAnnotationsByType(annotationClass);
    }

    /**
     * @since 1.5
     */
    public Annotation[] getAnnotations() {
        return getPackageInfo().getAnnotations();
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass) {
        return getPackageInfo().getDeclaredAnnotation(annotationClass);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationClass) {
        return getPackageInfo().getDeclaredAnnotationsByType(annotationClass);
    }

    /**
     * @since 1.5
     */
    public Annotation[] getDeclaredAnnotations()  {
        return getPackageInfo().getDeclaredAnnotations();
    }

    /**
     * Construct a package instance with the specified version
     * information.
     *
     * @param name the name of the package
     * @param spectitle the title of the specification
     * @param specversion the version of the specification
     * @param specvendor the organization that maintains the specification
     * @param impltitle the title of the implementation
     * @param implversion the version of the implementation
     * @param implvendor the organization that maintains the implementation
     * @param sealbase code source where this Package comes from
     * @param loader the class loader defining this Package
     */
    Package(String name,
            String spectitle, String specversion, String specvendor,
            String impltitle, String implversion, String implvendor,
            URL sealbase, ClassLoader loader)
    {
        Objects.requireNonNull(name);

        this.pkgName = name;
        this.implTitle = impltitle;
        this.implVersion = implversion;
        this.implVendor = implvendor;
        this.specTitle = spectitle;
        this.specVersion = specversion;
        this.specVendor = specvendor;
        this.sealBase = sealbase;
        this.loader = loader;
    }

    static boolean equals(Package p1, Package p2) {
        return Objects.equals(p1.pkgName, p2.pkgName) &&
               Objects.equals(p1.specTitle, p2.specTitle) &&
               Objects.equals(p1.specVersion, p2.specVersion) &&
               Objects.equals(p1.specVendor, p2.specVendor) &&
               Objects.equals(p1.implTitle, p2.implTitle) &&
               Objects.equals(p1.implVersion, p2.implVersion) &&
               Objects.equals(p1.implVendor, p2.implVendor) &&
               Objects.equals(p1.sealBase, p2.sealBase) &&
               p1.loader == p2.loader;
    }

    /*
     * Private storage for the package name and attributes.
     */
    private final String pkgName;
    private final String specTitle;
    private final String specVersion;
    private final String specVendor;
    private final String implTitle;
    private final String implVersion;
    private final String implVendor;
    private final URL sealBase;
    private final transient ClassLoader loader;
    private transient Class<?> packageInfo;
}
