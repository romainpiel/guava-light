# guava-light

Light version of guava with packages split in different targets (Designed for Android)

Work in progress...

## Motivations

Split guava into small modules

## Components

- guava-base : core classes (Predicate, Optional...) - size ~100Kb
- guava-primitives : primitives classes (Ints, Longs...) - size ~76Kb
- guava-math : math classes - size ~25Kb
- guava-collect-base : base classes for collect (Collections2, Lists, Iterables...) - size ~75Kb
   *Note that Immutable-related classes will appear in another package*

... more to come

## License

```
Copyright 2015 Romain Piel

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
