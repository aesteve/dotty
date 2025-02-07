package scala.tasty
package reflect

trait TypeOrBoundsOps extends Core {

  // ----- Types ----------------------------------------------------

  def typeOf[T: scala.quoted.Type]: Type

  given TypeOps: extension (self: Type) {

    /** Is `self` type the same as `that` type?
     *  This is the case iff `self <:< that` and `that <:< self`.
     */
    def =:=(that: Type)(given ctx: Context): Boolean = internal.Type_isTypeEq(self)(that)

    /** Is this type a subtype of that type? */
    def <:<(that: Type)(given ctx: Context): Boolean = internal.Type_isSubType(self)(that)

    /** Widen from singleton type to its underlying non-singleton
     *  base type by applying one or more `underlying` dereferences,
     *  Also go from => T to T.
     *  Identity for all other types. Example:
     *
     *  class Outer { class C ; val x: C }
     *  def o: Outer
     *  <o.x.type>.widen = o.C
     */
    def widen(given ctx: Context): Type = internal.Type_widen(self)

    /** Widen from TermRef to its underlying non-termref
     *  base type, while also skipping `=>T` types.
     */
    def widenTermRefExpr(given ctx: Context): Type = internal.Type_widenTermRefExpr(self)

    /** Follow aliases and dereferences LazyRefs, annotated types and instantiated
     *  TypeVars until type is no longer alias type, annotated type, LazyRef,
     *  or instantiated type variable.
     */
    def dealias(given ctx: Context): Type = internal.Type_dealias(self)

    /** A simplified version of this type which is equivalent wrt =:= to this type.
     *  Reduces typerefs, applied match types, and and or types.
     */
    def simplified(given ctx: Context): Type = internal.Type_simplified(self)

    def classSymbol(given ctx: Context): Option[Symbol] = internal.Type_classSymbol(self)
    def typeSymbol(given ctx: Context): Symbol = internal.Type_typeSymbol(self)
    def termSymbol(given ctx: Context): Symbol = internal.Type_termSymbol(self)
    def isSingleton(given ctx: Context): Boolean = internal.Type_isSingleton(self)
    def memberType(member: Symbol)(given ctx: Context): Type = internal.Type_memberType(self)(member)

    /** Is this type an instance of a non-bottom subclass of the given class `cls`? */
    def derivesFrom(cls: Symbol)(given ctx: Context): Boolean =
      internal.Type_derivesFrom(self)(cls)

    /** Is this type a function type?
     *
     *  @return true if the dealised type of `self` without refinement is `FunctionN[T1, T2, ..., Tn]`
     *
     *  @note The function
     *
     *     - returns true for `given Int => Int` and `erased Int => Int`
     *     - returns false for `List[Int]`, despite that `List[Int] <:< Int => Int`.
     */
    def isFunctionType(given ctx: Context): Boolean = internal.Type_isFunctionType(self)

    /** Is this type an implicit function type?
     *
     *  @see `isFunctionType`
     */
    def isImplicitFunctionType(given ctx: Context): Boolean = internal.Type_isImplicitFunctionType(self)

    /** Is this type an erased function type?
     *
     *  @see `isFunctionType`
     */
    def isErasedFunctionType(given ctx: Context): Boolean = internal.Type_isErasedFunctionType(self)

    /** Is this type a dependent function type?
     *
     *  @see `isFunctionType`
     */
    def isDependentFunctionType(given ctx: Context): Boolean = internal.Type_isDependentFunctionType(self)
  }

  given (given Context): IsInstanceOf[Type] = internal.isInstanceOfType

  object Type {
    def apply(clazz: Class[_])(given ctx: Context): Type =
      internal.Type_apply(clazz)
  }

  given (given Context): IsInstanceOf[ConstantType] = internal.isInstanceOfConstantType

  object ConstantType {
    def apply(x : Constant)(given ctx: Context): ConstantType = internal.ConstantType_apply(x)
    def unapply(x: ConstantType)(given ctx: Context): Option[Constant] = Some(x.constant)
  }

  given ConstantTypeOps: extension (self: ConstantType) {
    def constant(given ctx: Context): Constant = internal.ConstantType_constant(self)
  }

  given (given Context): IsInstanceOf[TermRef] = internal.isInstanceOfTermRef

  object TermRef {
    def apply(qual: TypeOrBounds, name: String)(given ctx: Context): TermRef =
      internal.TermRef_apply(qual, name)
    def unapply(x: TermRef)(given ctx: Context): Option[(TypeOrBounds /* Type | NoPrefix */, String)] =
      Some((x.qualifier, x.name))
  }

  given TermRefOps: extension (self: TermRef) {
    def qualifier(given ctx: Context): TypeOrBounds /* Type | NoPrefix */ = internal.TermRef_qualifier(self)
    def name(given ctx: Context): String = internal.TermRef_name(self)
  }

  given (given Context): IsInstanceOf[TypeRef] = internal.isInstanceOfTypeRef

  object TypeRef {
    def unapply(x: TypeRef)(given ctx: Context): Option[(TypeOrBounds /* Type | NoPrefix */, String)] =
      Some((x.qualifier, x.name))
  }

  given TypeRefOps: extension (self: TypeRef) {
    def qualifier(given ctx: Context): TypeOrBounds /* Type | NoPrefix */ = internal.TypeRef_qualifier(self)
    def name(given ctx: Context): String = internal.TypeRef_name(self)
  }

  given (given Context): IsInstanceOf[SuperType] = internal.isInstanceOfSuperType

  object SuperType {
    def unapply(x: SuperType)(given ctx: Context): Option[(Type, Type)] =
      Some((x.thistpe, x.supertpe))
  }

  given SuperTypeOps: extension (self: SuperType) {
    def thistpe(given ctx: Context): Type = internal.SuperType_thistpe(self)
    def supertpe(given ctx: Context): Type = internal.SuperType_supertpe(self)
  }

  given (given Context): IsInstanceOf[Refinement] = internal.isInstanceOfRefinement

  object Refinement {
    def apply(parent: Type, name: String, info: TypeOrBounds /* Type | TypeBounds */)(given ctx: Context): Refinement =
      internal.Refinement_apply(parent, name, info)

    def unapply(x: Refinement)(given ctx: Context): Option[(Type, String, TypeOrBounds /* Type | TypeBounds */)] =
      Some((x.parent, x.name, x.info))
  }

  given RefinementOps: extension (self: Refinement) {
    def parent(given ctx: Context): Type = internal.Refinement_parent(self)
    def name(given ctx: Context): String = internal.Refinement_name(self)
    def info(given ctx: Context): TypeOrBounds = internal.Refinement_info(self)
  }

  given (given Context): IsInstanceOf[AppliedType] = internal.isInstanceOfAppliedType

  object AppliedType {
    def apply(tycon: Type, args: List[TypeOrBounds])(given ctx: Context): AppliedType =
      internal.AppliedType_apply(tycon, args)
    def unapply(x: AppliedType)(given ctx: Context): Option[(Type, List[TypeOrBounds /* Type | TypeBounds */])] =
      Some((x.tycon, x.args))
  }

  given AppliedTypeOps: extension (self: AppliedType) {
    def tycon(given ctx: Context): Type = internal.AppliedType_tycon(self)
    def args(given ctx: Context): List[TypeOrBounds /* Type | TypeBounds */] = internal.AppliedType_args(self)
  }

  given (given Context): IsInstanceOf[AnnotatedType] = internal.isInstanceOfAnnotatedType

  object AnnotatedType {
    def apply(underlying: Type, annot: Term)(given ctx: Context): AnnotatedType =
      internal.AnnotatedType_apply(underlying, annot)
    def unapply(x: AnnotatedType)(given ctx: Context): Option[(Type, Term)] =
      Some((x.underlying, x.annot))
  }

  given AnnotatedTypeOps: extension (self: AnnotatedType) {
    def underlying(given ctx: Context): Type = internal.AnnotatedType_underlying(self)
    def annot(given ctx: Context): Term = internal.AnnotatedType_annot(self)
  }

  given (given Context): IsInstanceOf[AndType] = internal.isInstanceOfAndType

  object AndType {
    def apply(lhs: Type, rhs: Type)(given ctx: Context): AndType =
      internal.AndType_apply(lhs, rhs)
    def unapply(x: AndType)(given ctx: Context): Option[(Type, Type)] =
      Some((x.left, x.right))
  }

  given AndTypeOps: extension (self: AndType) {
    def left(given ctx: Context): Type = internal.AndType_left(self)
    def right(given ctx: Context): Type = internal.AndType_right(self)
  }

  given (given Context): IsInstanceOf[OrType] = internal.isInstanceOfOrType

  object OrType {
    def apply(lhs: Type, rhs: Type)(given ctx: Context): OrType = internal.OrType_apply(lhs, rhs)
    def unapply(x: OrType)(given ctx: Context): Option[(Type, Type)] =
      Some((x.left, x.right))
  }

  given OrTypeOps: extension (self: OrType) {
    def left(given ctx: Context): Type = internal.OrType_left(self)
    def right(given ctx: Context): Type = internal.OrType_right(self)
  }

  given (given Context): IsInstanceOf[MatchType] = internal.isInstanceOfMatchType

  object MatchType {
    def apply(bound: Type, scrutinee: Type, cases: List[Type])(given ctx: Context): MatchType =
      internal.MatchType_apply(bound, scrutinee, cases)
    def unapply(x: MatchType)(given ctx: Context): Option[(Type, Type, List[Type])] =
      Some((x.bound, x.scrutinee, x.cases))
  }

  given MatchTypeOps: extension (self: MatchType) {
    def bound(given ctx: Context): Type = internal.MatchType_bound(self)
    def scrutinee(given ctx: Context): Type = internal.MatchType_scrutinee(self)
    def cases(given ctx: Context): List[Type] = internal.MatchType_cases(self)
  }

  /**
   * An accessor for `scala.internal.MatchCase[_,_]`, the representation of a `MatchType` case.
    */
  def MatchCaseType(given Context): Type = {
    import scala.internal.MatchCase
    Type(classOf[MatchCase[_,_]])
  }

  given (given Context): IsInstanceOf[ByNameType] = internal.isInstanceOfByNameType

  object ByNameType {
    def apply(underlying: Type)(given ctx: Context): Type = internal.ByNameType_apply(underlying)
    def unapply(x: ByNameType)(given ctx: Context): Option[Type] = Some(x.underlying)
  }

  given ByNameTypeOps: extension (self: ByNameType) {
    def underlying(given ctx: Context): Type = internal.ByNameType_underlying(self)
  }

  given (given Context): IsInstanceOf[ParamRef] = internal.isInstanceOfParamRef

  object ParamRef {
    def unapply(x: ParamRef)(given ctx: Context): Option[(LambdaType[TypeOrBounds], Int)] =
      Some((x.binder, x.paramNum))
  }

  given ParamRefOps: extension (self: ParamRef) {
    def binder(given ctx: Context): LambdaType[TypeOrBounds] = internal.ParamRef_binder(self)
    def paramNum(given ctx: Context): Int = internal.ParamRef_paramNum(self)
  }

  given (given Context): IsInstanceOf[ThisType] = internal.isInstanceOfThisType

  object ThisType {
    def unapply(x: ThisType)(given ctx: Context): Option[Type] = Some(x.tref)
  }

  given ThisTypeOps: extension (self: ThisType) {
    def tref(given ctx: Context): Type = internal.ThisType_tref(self)
  }

  given (given Context): IsInstanceOf[RecursiveThis] = internal.isInstanceOfRecursiveThis

  object RecursiveThis {
    def unapply(x: RecursiveThis)(given ctx: Context): Option[RecursiveType] = Some(x.binder)
  }

  given RecursiveThisOps: extension (self: RecursiveThis) {
    def binder(given ctx: Context): RecursiveType = internal.RecursiveThis_binder(self)
  }

  given (given Context): IsInstanceOf[RecursiveType] = internal.isInstanceOfRecursiveType

  object RecursiveType {
    def unapply(x: RecursiveType)(given ctx: Context): Option[Type] = Some(x.underlying)
  }

  given RecursiveTypeOps: extension (self: RecursiveType) {
    def underlying(given ctx: Context): Type = internal.RecursiveType_underlying(self)
  }

  given (given Context): IsInstanceOf[MethodType] = internal.isInstanceOfMethodType

  object MethodType {
    def apply(paramNames: List[String])(paramInfosExp: MethodType => List[Type], resultTypeExp: MethodType => Type): MethodType =
      internal.MethodType_apply(paramNames)(paramInfosExp, resultTypeExp)

    def unapply(x: MethodType)(given ctx: Context): Option[(List[String], List[Type], Type)] =
      Some((x.paramNames, x.paramTypes, x.resType))
  }

  given MethodTypeOps: extension (self: MethodType) {
    def isImplicit: Boolean = internal.MethodType_isImplicit(self)
    def isErased: Boolean = internal.MethodType_isErased(self)
    def param(idx: Int)(given ctx: Context): Type = internal.MethodType_param(self, idx)
    def paramNames(given ctx: Context): List[String] = internal.MethodType_paramNames(self)
    def paramTypes(given ctx: Context): List[Type] = internal.MethodType_paramTypes(self)
    def resType(given ctx: Context): Type = internal.MethodType_resType(self)
  }

  given (given Context): IsInstanceOf[PolyType] = internal.isInstanceOfPolyType

  object PolyType {
    def apply(paramNames: List[String])(paramBoundsExp: PolyType => List[TypeBounds], resultTypeExp: PolyType => Type)(given ctx: Context): PolyType =
      internal.PolyType_apply(paramNames)(paramBoundsExp, resultTypeExp)
    def unapply(x: PolyType)(given ctx: Context): Option[(List[String], List[TypeBounds], Type)] =
      Some((x.paramNames, x.paramBounds, x.resType))
  }

  given PolyTypeOps: extension (self: PolyType) {
    def param(idx: Int)(given ctx: Context): Type = internal.PolyType_param(self, idx)
    def paramNames(given ctx: Context): List[String] = internal.PolyType_paramNames(self)
    def paramBounds(given ctx: Context): List[TypeBounds] = internal.PolyType_paramBounds(self)
    def resType(given ctx: Context): Type = internal.PolyType_resType(self)
  }

  given (given Context): IsInstanceOf[TypeLambda] = internal.isInstanceOfTypeLambda

  object TypeLambda {
    def apply(paramNames: List[String], boundsFn: TypeLambda => List[TypeBounds], bodyFn: TypeLambda => Type): TypeLambda =
      internal.TypeLambda_apply(paramNames, boundsFn, bodyFn)
    def unapply(x: TypeLambda)(given ctx: Context): Option[(List[String], List[TypeBounds], Type)] =
      Some((x.paramNames, x.paramBounds, x.resType))
  }

  given TypeLambdaOps: extension (self: TypeLambda) {
    def paramNames(given ctx: Context): List[String] = internal.TypeLambda_paramNames(self)
    def paramBounds(given ctx: Context): List[TypeBounds] = internal.TypeLambda_paramBounds(self)
    def param(idx: Int)(given ctx: Context) : Type = internal.TypeLambda_param(self, idx)
    def resType(given ctx: Context): Type = internal.TypeLambda_resType(self)
  }

  // ----- TypeBounds -----------------------------------------------

  given (given Context): IsInstanceOf[TypeBounds] = internal.isInstanceOfTypeBounds

  object TypeBounds {
    def apply(low: Type, hi: Type)(given ctx: Context): TypeBounds =
      internal.TypeBounds_apply(low, hi)
    def unapply(x: TypeBounds)(given ctx: Context): Option[(Type, Type)] = Some((x.low, x.hi))
  }

  given TypeBoundsOps: extension (self: TypeBounds) {
    def low(given ctx: Context): Type = internal.TypeBounds_low(self)
    def hi(given ctx: Context): Type = internal.TypeBounds_hi(self)
  }

  // ----- NoPrefix -------------------------------------------------

  given (given Context): IsInstanceOf[NoPrefix] = internal.isInstanceOfNoPrefix

  object NoPrefix
    def unapply(x: NoPrefix)(given ctx: Context): Boolean = true

}
