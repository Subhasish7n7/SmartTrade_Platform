package smarttrade.backend.Mappers;

public interface Mapper<A,B>{
    A mapTo(B b);
    B mapFrom(A a);
}
