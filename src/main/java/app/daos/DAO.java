package app.daos;

import java.util.List;

public interface DAO <T, I> {


    T create(T t);

    T getById(I id);

    T update(T t);

    boolean delete(I id);

    List<T> getAll();

}
