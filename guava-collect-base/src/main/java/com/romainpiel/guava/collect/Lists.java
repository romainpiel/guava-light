/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.romainpiel.guava.collect;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.romainpiel.guava.base.Function;
import com.romainpiel.guava.base.Objects;
import com.romainpiel.guava.math.IntMath;
import com.romainpiel.guava.primitives.Ints;

import java.io.Serializable;
import java.math.RoundingMode;
import java.util.AbstractList;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.romainpiel.guava.base.Preconditions.checkArgument;
import static com.romainpiel.guava.base.Preconditions.checkElementIndex;
import static com.romainpiel.guava.base.Preconditions.checkNotNull;
import static com.romainpiel.guava.collect.CollectPreconditions.checkNonnegative;

/**
 * Static utility methods pertaining to {@link List} instances. Also see this
 * class's counterparts {@link Sets}, {@link Maps} and {@link Queues}.
 *
 * <p>See the Guava User Guide article on <a href=
 * "http://code.google.com/p/guava-libraries/wiki/CollectionUtilitiesExplained#Lists">
 * {@code Lists}</a>.
 *
 * @author Kevin Bourrillion
 * @author Mike Bostock
 * @author Louis Wasserman
 * @since 2.0 (imported from Google Collections Library)
 */
public final class Lists {
  private Lists() {}

  // ArrayList

  /**
   * Creates a <i>mutable</i>, empty {@code ArrayList} instance (for Java 6 and
   * earlier).
   *
   * <p><b>Note:</b> if mutability is not required, use {@link
   * ImmutableList#of()} instead.
   *
   * <p><b>Note for Java 7 and later:</b> this method is now unnecessary and
   * should be treated as deprecated. Instead, use the {@code ArrayList}
   * {@linkplain ArrayList#ArrayList() constructor} directly, taking advantage
   * of the new <a href="http://goo.gl/iz2Wi">"diamond" syntax</a>.
   */
  public static <E> ArrayList<E> newArrayList() {
    return new ArrayList<E>();
  }

  /**
   * Creates a <i>mutable</i> {@code ArrayList} instance containing the given
   * elements.
   *
   * <p><b>Note:</b> essentially the only reason to use this method is when you
   * will need to add or remove elements later. Otherwise, for non-null elements
   * use {@link ImmutableList#of()} (for varargs) or {@link
   * ImmutableList#copyOf(Object[])} (for an array) instead. If any elements
   * might be null, or you need support for {@link List#set(int, Object)}, use
   * {@link Arrays#asList}.
   *
   * <p>Note that even when you do need the ability to add or remove, this method
   * provides only a tiny bit of syntactic sugar for {@code newArrayList(}{@link
   * Arrays#asList asList}{@code (...))}, or for creating an empty list then
   * calling {@link Collections#addAll}. This method is not actually very useful
   * and will likely be deprecated in the future.
   */
  public static <E> ArrayList<E> newArrayList(E... elements) {
    checkNotNull(elements); // for GWT
    // Avoid integer overflow when a large array is passed in
    int capacity = computeArrayListCapacity(elements.length);
    ArrayList<E> list = new ArrayList<E>(capacity);
    Collections.addAll(list, elements);
    return list;
  }

  @VisibleForTesting static int computeArrayListCapacity(int arraySize) {
    checkNonnegative(arraySize, "arraySize");

    // TODO(kevinb): Figure out the right behavior, and document it
    return Ints.saturatedCast(5L + arraySize + (arraySize / 10));
  }

  /**
   * Creates a <i>mutable</i> {@code ArrayList} instance containing the given
   * elements; a very thin shortcut for creating an empty list then calling
   * {@link Iterables#addAll}.
   *
   * <p><b>Note:</b> if mutability is not required and the elements are
   * non-null, use {@link ImmutableList#copyOf(Iterable)} instead. (Or, change
   * {@code elements} to be a {@link FluentIterable} and call
   * {@code elements.toList()}.)
   *
   * <p><b>Note for Java 7 and later:</b> if {@code elements} is a {@link
   * Collection}, you don't need this method. Use the {@code ArrayList}
   * {@linkplain ArrayList#ArrayList(Collection) constructor} directly, taking
   * advantage of the new <a href="http://goo.gl/iz2Wi">"diamond" syntax</a>.
   */
  public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements) {
    checkNotNull(elements); // for GWT
    // Let ArrayList's sizing logic work, if possible
    return (elements instanceof Collection)
        ? new ArrayList<E>(Collections2.cast(elements))
        : newArrayList(elements.iterator());
  }

  /**
   * Creates a <i>mutable</i> {@code ArrayList} instance containing the given
   * elements; a very thin shortcut for creating an empty list and then calling
   * {@link Iterators#addAll}.
   *
   * <p><b>Note:</b> if mutability is not required and the elements are
   * non-null, use {@link ImmutableList#copyOf(Iterator)} instead.
   */
  public static <E> ArrayList<E> newArrayList(Iterator<? extends E> elements) {
    ArrayList<E> list = newArrayList();
    Iterators.addAll(list, elements);
    return list;
  }

  /**
   * Creates an {@code ArrayList} instance backed by an array with the specified
   * initial size; simply delegates to {@link ArrayList#ArrayList(int)}.
   *
   * <p><b>Note for Java 7 and later:</b> this method is now unnecessary and
   * should be treated as deprecated. Instead, use {@code new }{@link
   * ArrayList#ArrayList(int) ArrayList}{@code <>(int)} directly, taking
   * advantage of the new <a href="http://goo.gl/iz2Wi">"diamond" syntax</a>.
   * (Unlike here, there is no risk of overload ambiguity, since the {@code
   * ArrayList} constructors very wisely did not accept varargs.)
   *
   * @param initialArraySize the exact size of the initial backing array for
   *     the returned array list ({@code ArrayList} documentation calls this
   *     value the "capacity")
   * @return a new, empty {@code ArrayList} which is guaranteed not to resize
   *     itself unless its size reaches {@code initialArraySize + 1}
   * @throws IllegalArgumentException if {@code initialArraySize} is negative
   */
  public static <E> ArrayList<E> newArrayListWithCapacity(
      int initialArraySize) {
    checkNonnegative(initialArraySize, "initialArraySize"); // for GWT.
    return new ArrayList<E>(initialArraySize);
  }

  /**
   * Creates an {@code ArrayList} instance to hold {@code estimatedSize}
   * elements, <i>plus</i> an unspecified amount of padding; you almost
   * certainly mean to call {@link #newArrayListWithCapacity} (see that method
   * for further advice on usage).
   *
   * <p><b>Note:</b> This method will soon be deprecated. Even in the rare case
   * that you do want some amount of padding, it's best if you choose your
   * desired amount explicitly.
   *
   * @param estimatedSize an estimate of the eventual {@link List#size()} of
   *     the new list
   * @return a new, empty {@code ArrayList}, sized appropriately to hold the
   *     estimated number of elements
   * @throws IllegalArgumentException if {@code estimatedSize} is negative
   */
  public static <E> ArrayList<E> newArrayListWithExpectedSize(
      int estimatedSize) {
    return new ArrayList<E>(computeArrayListCapacity(estimatedSize));
  }

  // LinkedList

  /**
   * Creates a <i>mutable</i>, empty {@code LinkedList} instance (for Java 6 and
   * earlier).
   *
   * <p><b>Note:</b> if you won't be adding any elements to the list, use {@link
   * ImmutableList#of()} instead.
   *
   * <p><b>Performance note:</b> {@link ArrayList} and {@link
   * java.util.ArrayDeque} consistently outperform {@code LinkedList} except in
   * certain rare and specific situations. Unless you have spent a lot of time
   * benchmarking your specific needs, use one of those instead.
   *
   * <p><b>Note for Java 7 and later:</b> this method is now unnecessary and
   * should be treated as deprecated. Instead, use the {@code LinkedList}
   * {@linkplain LinkedList#LinkedList() constructor} directly, taking advantage
   * of the new <a href="http://goo.gl/iz2Wi">"diamond" syntax</a>.
   */
  public static <E> LinkedList<E> newLinkedList() {
    return new LinkedList<E>();
  }

  /**
   * Creates a <i>mutable</i> {@code LinkedList} instance containing the given
   * elements; a very thin shortcut for creating an empty list then calling
   * {@link Iterables#addAll}.
   *
   * <p><b>Note:</b> if mutability is not required and the elements are
   * non-null, use {@link ImmutableList#copyOf(Iterable)} instead. (Or, change
   * {@code elements} to be a {@link FluentIterable} and call
   * {@code elements.toList()}.)
   *
   * <p><b>Performance note:</b> {@link ArrayList} and {@link
   * java.util.ArrayDeque} consistently outperform {@code LinkedList} except in
   * certain rare and specific situations. Unless you have spent a lot of time
   * benchmarking your specific needs, use one of those instead.
   *
   * <p><b>Note for Java 7 and later:</b> if {@code elements} is a {@link
   * Collection}, you don't need this method. Use the {@code LinkedList}
   * {@linkplain LinkedList#LinkedList(Collection) constructor} directly, taking
   * advantage of the new <a href="http://goo.gl/iz2Wi">"diamond" syntax</a>.
   */
  public static <E> LinkedList<E> newLinkedList(
      Iterable<? extends E> elements) {
    LinkedList<E> list = newLinkedList();
    Iterables.addAll(list, elements);
    return list;
  }

  /**
   * Creates an empty {@code CopyOnWriteArrayList} instance.
   *
   * <p><b>Note:</b> if you need an immutable empty {@link List}, use
   * {@link Collections#emptyList} instead.
   *
   * @return a new, empty {@code CopyOnWriteArrayList}
   * @since 12.0
   */
  public static <E> CopyOnWriteArrayList<E> newCopyOnWriteArrayList() {
    return new CopyOnWriteArrayList<E>();
  }

  /**
   * Creates a {@code CopyOnWriteArrayList} instance containing the given elements.
   *
   * @param elements the elements that the list should contain, in order
   * @return a new {@code CopyOnWriteArrayList} containing those elements
   * @since 12.0
   */
  public static <E> CopyOnWriteArrayList<E> newCopyOnWriteArrayList(
      Iterable<? extends E> elements) {
    // We copy elements to an ArrayList first, rather than incurring the
    // quadratic cost of adding them to the COWAL directly.
    Collection<? extends E> elementsCollection = (elements instanceof Collection)
        ? Collections2.cast(elements)
        : newArrayList(elements);
    return new CopyOnWriteArrayList<E>(elementsCollection);
  }

  /**
   * Returns an unmodifiable list containing the specified first element and
   * backed by the specified array of additional elements. Changes to the {@code
   * rest} array will be reflected in the returned list. Unlike {@link
   * Arrays#asList}, the returned list is unmodifiable.
   *
   * <p>This is useful when a varargs method needs to use a signature such as
   * {@code (Foo firstFoo, Foo... moreFoos)}, in order to avoid overload
   * ambiguity or to enforce a minimum argument count.
   *
   * <p>The returned list is serializable and implements {@link RandomAccess}.
   *
   * @param first the first element
   * @param rest an array of additional elements, possibly empty
   * @return an unmodifiable list containing the specified elements
   */
  public static <E> List<E> asList(@Nullable E first, E[] rest) {
    return new OnePlusArrayList<E>(first, rest);
  }

  /** @see Lists#asList(Object, Object[]) */
  private static class OnePlusArrayList<E> extends AbstractList<E>
      implements Serializable, RandomAccess {
    final E first;
    final E[] rest;

    OnePlusArrayList(@Nullable E first, E[] rest) {
      this.first = first;
      this.rest = checkNotNull(rest);
    }
    @Override public int size() {
      return rest.length + 1;
    }
    @Override public E get(int index) {
      // check explicitly so the IOOBE will have the right message
      checkElementIndex(index, size());
      return (index == 0) ? first : rest[index - 1];
    }
    private static final long serialVersionUID = 0;
  }

  /**
   * Returns an unmodifiable list containing the specified first and second
   * element, and backed by the specified array of additional elements. Changes
   * to the {@code rest} array will be reflected in the returned list. Unlike
   * {@link Arrays#asList}, the returned list is unmodifiable.
   *
   * <p>This is useful when a varargs method needs to use a signature such as
   * {@code (Foo firstFoo, Foo secondFoo, Foo... moreFoos)}, in order to avoid
   * overload ambiguity or to enforce a minimum argument count.
   *
   * <p>The returned list is serializable and implements {@link RandomAccess}.
   *
   * @param first the first element
   * @param second the second element
   * @param rest an array of additional elements, possibly empty
   * @return an unmodifiable list containing the specified elements
   */
  public static <E> List<E> asList(
      @Nullable E first, @Nullable E second, E[] rest) {
    return new TwoPlusArrayList<E>(first, second, rest);
  }

  /** @see Lists#asList(Object, Object, Object[]) */
  private static class TwoPlusArrayList<E> extends AbstractList<E>
      implements Serializable, RandomAccess {
    final E first;
    final E second;
    final E[] rest;

    TwoPlusArrayList(@Nullable E first, @Nullable E second, E[] rest) {
      this.first = first;
      this.second = second;
      this.rest = checkNotNull(rest);
    }
    @Override public int size() {
      return rest.length + 2;
    }
    @Override public E get(int index) {
      switch (index) {
        case 0:
          return first;
        case 1:
          return second;
        default:
          // check explicitly so the IOOBE will have the right message
          checkElementIndex(index, size());
          return rest[index - 2];
      }
    }
    private static final long serialVersionUID = 0;
  }


  /**
   * Returns a list that applies {@code function} to each element of {@code
   * fromList}. The returned list is a transformed view of {@code fromList};
   * changes to {@code fromList} will be reflected in the returned list and vice
   * versa.
   *
   * <p>Since functions are not reversible, the transform is one-way and new
   * items cannot be stored in the returned list. The {@code add},
   * {@code addAll} and {@code set} methods are unsupported in the returned
   * list.
   *
   * <p>The function is applied lazily, invoked when needed. This is necessary
   * for the returned list to be a view, but it means that the function will be
   * applied many times for bulk operations like {@link List#contains} and
   * {@link List#hashCode}. For this to perform well, {@code function} should be
   * fast. To avoid lazy evaluation when the returned list doesn't need to be a
   * view, copy the returned list into a new list of your choosing.
   *
   * <p>If {@code fromList} implements {@link RandomAccess}, so will the
   * returned list. The returned list is threadsafe if the supplied list and
   * function are.
   *
   * <p>If only a {@code Collection} or {@code Iterable} input is available, use
   * {@link Collections2#transform} or {@link Iterables#transform}.
   *
   * <p><b>Note:</b> serializing the returned list is implemented by serializing
   * {@code fromList}, its contents, and {@code function} -- <i>not</i> by
   * serializing the transformed values. This can lead to surprising behavior,
   * so serializing the returned list is <b>not recommended</b>. Instead,
   * copy the list using {@link ImmutableList#copyOf(Collection)} (for example),
   * then serialize the copy. Other methods similar to this do not implement
   * serialization at all for this reason.
   */
  public static <F, T> List<T> transform(
      List<F> fromList, Function<? super F, ? extends T> function) {
    return (fromList instanceof RandomAccess)
        ? new TransformingRandomAccessList<F, T>(fromList, function)
        : new TransformingSequentialList<F, T>(fromList, function);
  }

  /**
   * Implementation of a sequential transforming list.
   *
   * @see Lists#transform
   */
  private static class TransformingSequentialList<F, T>
      extends AbstractSequentialList<T> implements Serializable {
    final List<F> fromList;
    final Function<? super F, ? extends T> function;

    TransformingSequentialList(
        List<F> fromList, Function<? super F, ? extends T> function) {
      this.fromList = checkNotNull(fromList);
      this.function = checkNotNull(function);
    }
    /**
     * The default implementation inherited is based on iteration and removal of
     * each element which can be overkill. That's why we forward this call
     * directly to the backing list.
     */
    @Override public void clear() {
      fromList.clear();
    }
    @Override public int size() {
      return fromList.size();
    }
    @Override public ListIterator<T> listIterator(final int index) {
      return new TransformedListIterator<F, T>(fromList.listIterator(index)) {
        @Override
        T transform(F from) {
          return function.apply(from);
        }
      };
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Implementation of a transforming random access list. We try to make as many
   * of these methods pass-through to the source list as possible so that the
   * performance characteristics of the source list and transformed list are
   * similar.
   *
   * @see Lists#transform
   */
  private static class TransformingRandomAccessList<F, T>
      extends AbstractList<T> implements RandomAccess, Serializable {
    final List<F> fromList;
    final Function<? super F, ? extends T> function;

    TransformingRandomAccessList(
        List<F> fromList, Function<? super F, ? extends T> function) {
      this.fromList = checkNotNull(fromList);
      this.function = checkNotNull(function);
    }
    @Override public void clear() {
      fromList.clear();
    }
    @Override public T get(int index) {
      return function.apply(fromList.get(index));
    }
    @Override public Iterator<T> iterator() {
      return listIterator();
    }
    @Override public ListIterator<T> listIterator(int index) {
      return new TransformedListIterator<F, T>(fromList.listIterator(index)) {
        @Override
        T transform(F from) {
          return function.apply(from);
        }
      };
    }
    @Override public boolean isEmpty() {
      return fromList.isEmpty();
    }
    @Override public T remove(int index) {
      return function.apply(fromList.remove(index));
    }
    @Override public int size() {
      return fromList.size();
    }
    private static final long serialVersionUID = 0;
  }

  /**
   * Returns consecutive {@linkplain List#subList(int, int) sublists} of a list,
   * each of the same size (the final list may be smaller). For example,
   * partitioning a list containing {@code [a, b, c, d, e]} with a partition
   * size of 3 yields {@code [[a, b, c], [d, e]]} -- an outer list containing
   * two inner lists of three and two elements, all in the original order.
   *
   * <p>The outer list is unmodifiable, but reflects the latest state of the
   * source list. The inner lists are sublist views of the original list,
   * produced on demand using {@link List#subList(int, int)}, and are subject
   * to all the usual caveats about modification as explained in that API.
   *
   * @param list the list to return consecutive sublists of
   * @param size the desired size of each sublist (the last may be
   *     smaller)
   * @return a list of consecutive sublists
   * @throws IllegalArgumentException if {@code partitionSize} is nonpositive
   */
  public static <T> List<List<T>> partition(List<T> list, int size) {
    checkNotNull(list);
    checkArgument(size > 0);
    return (list instanceof RandomAccess)
        ? new RandomAccessPartition<T>(list, size)
        : new Partition<T>(list, size);
  }

  private static class Partition<T> extends AbstractList<List<T>> {
    final List<T> list;
    final int size;

    Partition(List<T> list, int size) {
      this.list = list;
      this.size = size;
    }

    @Override public List<T> get(int index) {
      checkElementIndex(index, size());
      int start = index * size;
      int end = Math.min(start + size, list.size());
      return list.subList(start, end);
    }

    @Override public int size() {
      return IntMath.divide(list.size(), size, RoundingMode.CEILING);
    }

    @Override public boolean isEmpty() {
      return list.isEmpty();
    }
  }

  private static class RandomAccessPartition<T> extends Partition<T>
      implements RandomAccess {
    RandomAccessPartition(List<T> list, int size) {
      super(list, size);
    }
  }

  /**
   * An implementation of {@link List#hashCode()}.
   */
  static int hashCodeImpl(List<?> list) {
    // TODO(user): worth optimizing for RandomAccess?
    int hashCode = 1;
    for (Object o : list) {
      hashCode = 31 * hashCode + (o == null ? 0 : o.hashCode());

      hashCode = ~~hashCode;
      // needed to deal with GWT integer overflow
    }
    return hashCode;
  }

  /**
   * An implementation of {@link List#equals(Object)}.
   */
  static boolean equalsImpl(List<?> list, @Nullable Object object) {
    if (object == checkNotNull(list)) {
      return true;
    }
    if (!(object instanceof List)) {
      return false;
    }

    List<?> o = (List<?>) object;

    return list.size() == o.size()
        && Iterators.elementsEqual(list.iterator(), o.iterator());
  }

  /**
   * An implementation of {@link List#addAll(int, Collection)}.
   */
  static <E> boolean addAllImpl(
      List<E> list, int index, Iterable<? extends E> elements) {
    boolean changed = false;
    ListIterator<E> listIterator = list.listIterator(index);
    for (E e : elements) {
      listIterator.add(e);
      changed = true;
    }
    return changed;
  }

  /**
   * An implementation of {@link List#indexOf(Object)}.
   */
  static int indexOfImpl(List<?> list, @Nullable Object element) {
    ListIterator<?> listIterator = list.listIterator();
    while (listIterator.hasNext()) {
      if (Objects.equal(element, listIterator.next())) {
        return listIterator.previousIndex();
      }
    }
    return -1;
  }

  /**
   * An implementation of {@link List#lastIndexOf(Object)}.
   */
  static int lastIndexOfImpl(List<?> list, @Nullable Object element) {
    ListIterator<?> listIterator = list.listIterator(list.size());
    while (listIterator.hasPrevious()) {
      if (Objects.equal(element, listIterator.previous())) {
        return listIterator.nextIndex();
      }
    }
    return -1;
  }

  /**
   * Returns an implementation of {@link List#listIterator(int)}.
   */
  static <E> ListIterator<E> listIteratorImpl(List<E> list, int index) {
    return new AbstractListWrapper<E>(list).listIterator(index);
  }

  /**
   * An implementation of {@link List#subList(int, int)}.
   */
  static <E> List<E> subListImpl(
      final List<E> list, int fromIndex, int toIndex) {
    List<E> wrapper;
    if (list instanceof RandomAccess) {
      wrapper = new RandomAccessListWrapper<E>(list) {
        @Override public ListIterator<E> listIterator(int index) {
          return backingList.listIterator(index);
        }

        private static final long serialVersionUID = 0;
      };
    } else {
      wrapper = new AbstractListWrapper<E>(list) {
        @Override public ListIterator<E> listIterator(int index) {
          return backingList.listIterator(index);
        }

        private static final long serialVersionUID = 0;
      };
    }
    return wrapper.subList(fromIndex, toIndex);
  }

  private static class AbstractListWrapper<E> extends AbstractList<E> {
    final List<E> backingList;

    AbstractListWrapper(List<E> backingList) {
      this.backingList = checkNotNull(backingList);
    }

    @Override public void add(int index, E element) {
      backingList.add(index, element);
    }

    @Override public boolean addAll(int index, Collection<? extends E> c) {
      return backingList.addAll(index, c);
    }

    @Override public E get(int index) {
      return backingList.get(index);
    }

    @Override public E remove(int index) {
      return backingList.remove(index);
    }

    @Override public E set(int index, E element) {
      return backingList.set(index, element);
    }

    @Override public boolean contains(Object o) {
      return backingList.contains(o);
    }

    @Override public int size() {
      return backingList.size();
    }
  }

  private static class RandomAccessListWrapper<E>
      extends AbstractListWrapper<E> implements RandomAccess {
    RandomAccessListWrapper(List<E> backingList) {
      super(backingList);
    }
  }

  /**
   * Used to avoid http://bugs.sun.com/view_bug.do?bug_id=6558557
   */
  static <T> List<T> cast(Iterable<T> iterable) {
    return (List<T>) iterable;
  }
}
