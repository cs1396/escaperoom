package cs1396.escaperoom.engine.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import cs1396.escaperoom.engine.types.Result.Err;
import cs1396.escaperoom.engine.types.Result.Ok;

public class SemVer implements Comparable<SemVer> {
  private VersionCore versionCore;
  private Optional<PreRelease> preRelease = Optional.empty();
  private Optional<Build> build = Optional.empty();


  //---------------------------------------------------------------------------
  //
  // Comparable
  //
  //---------------------------------------------------------------------------
  @Override
  public int compareTo(SemVer sv) {
    

    // Precedence is determined by the first difference when comparing
    // each of these identifiers from left to right as follows:
    // Major, minor, and patch versions are always compared numerically.
    int ret = 0;
    if ((ret = versionCore.major().value().compareTo(sv.versionCore.major().value())) != 0){
      return ret;
    }
    if ((ret = versionCore.minor().value().compareTo(sv.versionCore.minor().value())) != 0){
      return ret;
    }
    if ((ret = versionCore.patch().value().compareTo(sv.versionCore.patch().value())) != 0){
      return ret;
    }

    if (preRelease.isEmpty() && sv.preRelease.isEmpty()) return 0;

    // When major, minor, and patch are equal, a pre-release version has
    // lower precedence than a normal version:
    if (preRelease.isPresent() && sv.preRelease.isEmpty()) {
      return -1;
    }
    if (preRelease.isEmpty() && sv.preRelease.isPresent()) {
      return 1;
    }

    return preRelease.get().compareTo(sv.preRelease.get());
  }

  //---------------------------------------------------------------------------
  //
  // Public API 
  //
  //---------------------------------------------------------------------------
  public static Result<SemVer, ParseError> parse(String tok){
    Stack<Character> toks = stackifyString(tok);

    var core = VersionCore.parse(toks);
    if (toks.isEmpty() || core.isErr()) return core.map(t -> new SemVer(t));

    // SAFETY: core cannot be Err
    SemVer semVer = new SemVer(core.unwrap()); 

    while (true){
      if (toks.isEmpty()){
        return new Ok<>(semVer);
      }

      switch (toks.peek()){
        case '-':
          if (semVer.preRelease.isPresent()) return fail("\"+\"<build> or EOS", toks);

          toks.pop();

          var preRelease = PreRelease.parse(toks);
          if (preRelease.isErr()) return fail("<pre-release>", toks);
          else semVer.preRelease = preRelease.ok();

          break;
        case '+':
          if (semVer.build.isPresent()) return fail("EOS", toks);

          toks.pop();

          var build = Build.parse(toks);
          if (build.isErr()) return fail("<build>", toks);
          else semVer.build = build.ok();

          break;
        default:
          return fail("\"-\" for pre-release or \"+\" for build", toks);
      }
    }
  }

  //---------------------------------------------------------------------------
  //
  // Construction 
  //
  //---------------------------------------------------------------------------
  public SemVer(Long major, Long minor, Long patch){
    this(new VersionCore(major, minor, patch));
  }


  private SemVer(VersionCore versionCore){
    this.versionCore = versionCore;
  }

  public static class Builder {
    VersionCore core = new VersionCore(0L,0L,0L);
    PreRelease preRelease = null;
    Build build = null;

    public Builder version(Long major, Long minor, Long patch){
      core = new VersionCore(major, minor, patch);
      return this;
    }

    public Builder preReleaseIDs(String ... preReleaseIDs){
      preRelease = new PreRelease(
          Arrays.stream(preReleaseIDs)
          .map(s -> PreReleaseID
            .parse(stackifyString(s))
            .expect("Invalid pre-release identifier \"" + s +"\"")
          )
          .toList()
        );

      return this;
    }

    public Builder buildIDs(String ... buildIDs){
      build = new Build(
          Arrays.stream(buildIDs)
          .map(s -> new BuildID(s))
          .toList()
        );

      return this;
    }

    public SemVer build(){
      SemVer sv = new SemVer(core);
      if (preRelease != null) sv.preRelease = Optional.of(preRelease);
      if (build != null) sv.build = Optional.of(build);
      return sv;
    }
  }

  //---------------------------------------------------------------------------
  //
  // Utils 
  //
  //---------------------------------------------------------------------------
  private static Stack<Character> cloneStack(Stack<Character> toks){
    Stack<Character> newStack = new Stack<>();
    newStack.addAll(toks);
    return newStack;
  }

  private static Stack<Character> stackifyString(String tok){
    Stack<Character> toks = new Stack<>();
    StringBuffer buf = new StringBuffer(tok);
    buf.reverse().chars().forEach(c -> toks.push((char)c));
    return toks;
  }


  public static record ParseError(String reason){
    @Override
    public final String toString() {
      return reason();
    }
  };

  // https://semver.org/
  static final Set<Character> LETTER = Set.of(
    'A' , 'B' , 'C' , 'D' , 'E' , 'F' , 'G' , 'H' , 'I' , 'J' ,
    'K' , 'L' , 'M' , 'N' , 'O' , 'P' , 'Q' , 'R' , 'S' , 'T' ,
    'U' , 'V' , 'W' , 'X' , 'Y' , 'Z' , 'a' , 'b' , 'c' , 'd' ,
    'e' , 'f' , 'g' , 'h' , 'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
    'o' , 'p' , 'q' , 'r' , 's' , 't' , 'u' , 'v' , 'w' , 'x' ,
    'y' , 'z'
  );
  static final Set<Character> POSITIVE_DIGITS = Set.of('1','2','3','4','5','6','7', '8', '9');
  static final Set<Character> DIGITS = Set.of('0', '1','2','3','4','5','6','7', '8', '9');

  private static<T>  Err<T, ParseError> fail(String expected, Stack<Character> tokStream){
    StringBuilder builder = new StringBuilder();
    while(!tokStream.isEmpty()) builder.append(tokStream.pop());

    return new Err<>(new ParseError("Expected " + expected + " got " + builder.toString()));
  }

  //---------------------------------------------------------------------------
  //
  // Components 
  //
  //---------------------------------------------------------------------------
  private record AlphaNumericIdentifier(String inner){

    static boolean addNonDigit(Stack<Character> inp, StringBuilder mut){
      if (inp.isEmpty()) return false;
      char first = inp.peek();
      if (LETTER.contains(first) || first == '-'){
        inp.pop();
        mut.append(first);
        return true;
      } 
      return false;
    }

    static boolean addDigit(Stack<Character> inp, StringBuilder mut){
      if (inp.isEmpty()) return false;

      char first = inp.peek();
      if (POSITIVE_DIGITS.contains(first) || first == '0'){
        inp.pop();
        mut.append(first);
        return true;
      } 
      return false;
    }


    static class Identifier {
      IdentifierType type;
      public Identifier(IdentifierType type) { this.type = type; }
      public void record(IdentifierType type){
        switch (type){
          case AlphaNumeric:
            this.type = type;
            break;
          case Numeric:
            if (this.type == IdentifierType.AlphaNumeric) return;
            else this.type = type;
            break;
          default:
            break;
        }
      }
    }

    static IdentifierType addIdentifierCharacters(Stack<Character> toks, StringBuilder mut){
      Optional<IdentifierType> added = addIdentifierCharacter(toks, mut);
      if (added.isEmpty()) return IdentifierType.None;

      Identifier identifier = new Identifier(IdentifierType.None);
      do {
        identifier.record(added.get());

        added = addIdentifierCharacter(toks, mut);
      } while(added.isPresent());

      return identifier.type;
    }

    static Optional<IdentifierType> addIdentifierCharacter(Stack<Character> inp, StringBuilder mut){
      if (addDigit(inp, mut)) return Optional.of(IdentifierType.Numeric);
      if (addNonDigit(inp, mut)) return Optional.of(IdentifierType.AlphaNumeric);

      return Optional.empty();
    }

    static Result<AlphaNumericIdentifier, ParseError> parse(Stack<Character> toks){
      if (toks.isEmpty()) return fail("<alphanumeric identifier>", toks);
      
      // <alphanumeric identifier> ::= <non-digit>
      //                             | <non-digit> <identifier characters>
      //                             | <identifier characters> <non-digit>
      //                             | <identifier characters> <non-digit> <identifier characters>

      StringBuilder builder = new StringBuilder();

      if (addNonDigit(toks, builder)){
        addIdentifierCharacters(toks, builder);
        return new Ok<>(new AlphaNumericIdentifier(builder.toString()));
      } 

      switch (addIdentifierCharacters(toks, builder)){
        case AlphaNumeric:
          return new Ok<>(new AlphaNumericIdentifier(builder.toString()));
        case Numeric:
          if (!addNonDigit(toks, builder)) return fail("<alphanumeric identifier>", toks);
          return new Ok<>(new AlphaNumericIdentifier(builder.toString()));
        case None:
          return fail("<alphanumeric identifier>", toks);
      }
      return fail("<alphanumeric identifier>", toks);
    }
  }

  private record NumericIdentifier(Long value) {
    static Result<NumericIdentifier, ParseError> parse(Stack<Character> toks){
      if (toks.isEmpty()) return fail("<numeric identifier>", toks);

      // <numeric identifier> ::= "0"
      //                        | <positive digit>
      //                        | <positive digit> <digits>
      if (toks.peek() == '0') {
        toks.pop();
        return new Ok<>(new NumericIdentifier(0L));
      }

      StringBuilder str = new StringBuilder();

      // Must be true: toks does not start with zero 
      while(!toks.isEmpty() && DIGITS.contains(toks.peek())) str.append(toks.pop());

      if (str.isEmpty()) return fail("<numeric identifier>", toks);

      return new Ok<>(new NumericIdentifier(Long.parseLong(str.toString())));
    }

    @Override
    public final boolean equals(Object arg0) {
      if (arg0 instanceof Long l){
        return l.equals(this.value);
      } else if (arg0 instanceof NumericIdentifier ni){
        return ni.value.equals(this.value);
      }

      return false;
    }
  }


  private record PreRelease(List<PreReleaseID> dotSepPreRelease) implements Comparable<PreRelease>{
    static Result<PreRelease, ParseError> parse(Stack<Character> inp) {
      List<PreReleaseID> ids = new ArrayList<>();

      Result<PreReleaseID, ParseError> id;
      do {
        id = PreReleaseID.parse(inp);
        if (id.isErr()){

          if (!inp.isEmpty() && inp.peek() == '+' && !ids.isEmpty()){
            return new Ok<>(new PreRelease(ids));
          }

          if (inp.isEmpty()) return id.map(t -> null);
        }

        ids.add(id.unwrap());

        if (!inp.isEmpty() && inp.peek() == '.'){
          inp.pop();
          continue;
        }

        return new Ok<>(new PreRelease(ids));
      } while (true);
    }

    @Override
    public final boolean equals(Object arg0) {
      if (arg0 instanceof PreRelease pr){
        return this.dotSepPreRelease.equals(pr.dotSepPreRelease);
      }
      return false;
    }

    @Override
    public int compareTo(PreRelease pr) {
      Integer i = 0, j = 0;

      while (i < dotSepPreRelease.size() && j < pr.dotSepPreRelease.size()){
        PreReleaseID ourID = dotSepPreRelease.get(i);
        PreReleaseID prID = pr.dotSepPreRelease.get(i);

        if (ourID.type() != prID.type()){
          return ourID.type().compareTo(prID.type()); 
        }

        int ret = switch (ourID.type()){
          case Numeric -> Long.valueOf(ourID.id).compareTo(Long.valueOf(prID.id));
          case AlphaNumeric -> ourID.id.compareTo(prID.id);
          default -> throw new IllegalStateException("Invalid Identifier type");
        };

        if (ret != 0) return ret;

        i++;
        j++;
      }

      if (i == dotSepPreRelease.size() && j == pr.dotSepPreRelease.size()) 
        return 0;

      if (i < dotSepPreRelease.size()) return 1;
      else return -1;
    }
  }

  private record PreReleaseID(String id, IdentifierType type){
    static Result<PreReleaseID, ParseError> parse(Stack<Character> toks) {
      Stack<Character> tryToks = cloneStack(toks);

      if (NumericIdentifier.parse(tryToks).isOk() &&
          (tryToks.isEmpty() ||
           tryToks.peek() == '.' ||
           tryToks.peek() == '+')
      ){
        return NumericIdentifier.parse(toks)
          .map(ani -> new PreReleaseID(Long.toString(ani.value), IdentifierType.Numeric));
      }

      return AlphaNumericIdentifier.parse(toks).map(ani -> new PreReleaseID(ani.inner, IdentifierType.AlphaNumeric));
    }
  }

  private record Build(List<BuildID> dotSepBuild){
    static Result<Build, ParseError> parse(Stack<Character> inp) {
      List<BuildID> ids = new ArrayList<>();

      Result<BuildID, ParseError> id;
      do {
        id = BuildID.parse(inp);
        if (id.isErr()) return id.map(t -> null);

        ids.add(id.unwrap());

        if (!inp.isEmpty() && inp.peek() == '.'){
          inp.pop();
          continue;
        }

        return new Ok<>(new Build(ids));
      } while (true);
    }

    @Override
    public final boolean equals(Object arg0) {
      if (arg0 instanceof Build b){
        return this.dotSepBuild.equals(b.dotSepBuild);
      }
      return false;
    }
  }

  private record BuildID(String id){
    static Result<BuildID, ParseError> parse(Stack<Character> toks) {

      Stack<Character> tryStack = cloneStack(toks);

      if (AlphaNumericIdentifier.parse(tryStack).isOk()) 
        return AlphaNumericIdentifier.parse(toks).map(ani -> new BuildID(ani.inner));

      return parseDigits(toks);
    }

    static Result<BuildID, ParseError> parseDigits(Stack<Character> toks) {
      if (toks.isEmpty()) return fail("<digits>", toks);

      StringBuilder str = new StringBuilder();

      while(!toks.isEmpty() && (DIGITS.contains(toks.peek()))) str.append(toks.pop());

      if (str.isEmpty()) return fail("<digits>", toks);

      return new Ok<>(new BuildID(str.toString()));
    }
  }

  private record VersionCore(NumericIdentifier major, NumericIdentifier minor, NumericIdentifier patch) {
    public VersionCore(Long major, Long minor, Long patch){
      this(new NumericIdentifier(major), new NumericIdentifier(minor), new NumericIdentifier(patch));
    }

    static Result<VersionCore, ParseError> parse(Stack<Character> toks){
      var major = NumericIdentifier.parse(toks); 
      if (major.isErr()) return major.map(a -> null);

      if (toks.isEmpty() || toks.peek() != '.') return fail("\".\" after major version", toks);
      toks.pop();

      var minor = NumericIdentifier.parse(toks); 
      if (minor.isErr()) return minor.map(a -> null);

      if (toks.isEmpty() || toks.peek() != '.') return fail("\".\" after minor version", toks);
      toks.pop();

      var patch = NumericIdentifier.parse(toks); 
      if (patch.isErr()) return patch.map(a -> null);

      VersionCore core = new VersionCore(major.unwrap(), minor.unwrap(), patch.unwrap());
      return new Ok<>(core);
    }

    @Override
    public final String toString() {
      return String.format("%d.%d.%d", major.value, minor.value, patch.value);
    }

    @Override
    public final boolean equals(Object arg0) {
      if (arg0 instanceof VersionCore vc){
        return vc.major().value().equals(this.major().value()) &&
               vc.minor().value().equals(this.minor().value()) &&
               vc.patch().value().equals(this.patch().value());
      }
      return false;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SemVer sv){
      return this.versionCore.equals(sv.versionCore) && 
             this.preRelease.equals(sv.preRelease)   &&
             this.build.equals(sv.build);
    }
    return false;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(this.versionCore.toString());

    preRelease.ifPresent(pr -> {
      if (pr.dotSepPreRelease.isEmpty()) return;

      builder.append('-');
      int i = 0; 
      int size = pr.dotSepPreRelease.size();
      for (PreReleaseID id : pr.dotSepPreRelease){
        builder.append(id.id);
        if (i < size - 1) builder.append('.');
        i++;
      }
    });
    return builder.toString();
  }

  private enum IdentifierType {
    // NOTE: 
    // Do not change the order of these elements.
    // Precedence for pre-releases considers identifier type.
    // p.s. We cannot override compareTo for enums :(
    None,
    Numeric,
    AlphaNumeric;
  }
}
