# History of futils releases

## 1.2.2 (2016-05-30)

- Fixed metadata hash maps positions of defs in a few places.

## 1.2.1 (2015-12-18)

- Added `:use-seq` and `:apply-raw` transforming options to `futils.named/comp`.

## 1.2.0 (2015-12-18)

- Added `futils.named/comp` (function composition).
- Added `futils.named/identity` (identity function).
- Added `futils.named/apply` (named arguments application).
- Documentation updated and reformatted (removed subsubsections).

## 1.0.2 (2015-12-06)

- Added initialization function and dev-mode detection.

## 1.0.1 (2015-12-06)

- Documentation updated.

## 1.0.0 (2015-12-05)

- Argument counting and relaxing functions are now using lazy sequences
  instead of sets to work with arity counters (most of functions doesn't have
  that much arities for sets to be computationally efficient). Changes affect:

    - `futils.args/argc`,
    - `futils.args/relax`,
    - `futils.args/relax*`.

## 0.8.0 (2015-12-02)

- Nameization does not use set operations to find matching arity.
- Nameization properly handles `nil` values given as default named arguments.

## 0.7.1 (2015-12-01)

- The `nameize` macro allows to omit default values maps in mapping pairs.

## 0.7.0 (2015-11-30)

- Functions spread across namespaces: `utils`, `named` and `args`.
- Added multiple arities support to `nameize` macro and `nameize*` function.
- Added tests for nameization.
- Documentation updated.

## 0.6.0 (2015-11-05)

- Added nameization support (`nameize` and `nameize*`).
- Added `if->` and `if-not->` utility macros.
- Function `args-relax` renamed to `relax*`.
- Improved assertions in `relax*`.
- Bug fixes for verbose mode of `relax*`.

## 0.4.0 (2015-10-30)

- Fixes in utility functions.
- Function `frelax` renamed to `relax`.
- Function `ensure-fn` renamed to `require-fn`.
- Documentation updated.
- Dependencies updated.
- Added tests.

## 0.3.0 (2015-10-28)

- Function `any?` redefined and fixed.
- Function `macroize-args` made private and fixed.
- Added more type hints.

## 0.1.1 (2015-10-28)

- Added `mapply` and `frepeat` functions.
- The padding function and padding value support to `frelax` and `args-relax`.
- Type hints adjusted.
- Added documentation.

## 0.1.0 (2015-10-27)

- First public release.
