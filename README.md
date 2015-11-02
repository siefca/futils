# futils – Function Utilities Library

[![Build Status](https://travis-ci.org/siefca/futils.png?branch=master)](https://travis-ci.org/siefca/futils)

This library defines forms that abstract some common operations on functions,
like counting their arguments, creating wrappers, passing proper number of
arguments and so on.

## Installation

The current release is 0.5.2. To use futils in your project, add the following
to the `dependencies` section of `project.clj`:

```
[pl.randomseed/futils "0.5.2"]
```

## Components

Currently prvided macros and functions are:

* [`argc`][argc] – counts arguments a function takes (for all arities),
* [`relax`][relax] – wraps a function in a way that it accepts any number of
  arguments,
* [`args-relax`][args-relax] – like `relax` but it requires to explicitly
  describe the accepted arities,
* [`frepeat`][frepeat] – creates a sequence of returned values using a function
  with named parameters,
* [`mapply`][mapply] – works like apply but for named arguments.

## Documentation

Full documentation with usage examples is available on:

* https://randomseed.pl/software/futils/

## Examples

Examples can be found in the `examples` subdirectory of the repo.

## License

Copyright © 2015 Paweł Wilk

Futils is copyrighted software owned by Paweł Wilk (pw@gnu.org). You may
redistribute and/or modify this software as long as you comply with the terms of
the [GNU Lesser General Public License][LICENSE] (version 3).

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

[NEWS.md]:    https://github.com/siefca/futils/blob/master/NEWS.md
[LICENSE]:    https://github.com/siefca/futils/blob/master/LICENSE
[argc]:       https://randomseed.pl/software/futils/#argc
[relax]:      https://randomseed.pl/software/futils/#relax
[args-relax]: https://randomseed.pl/software/futils/#args-relax
[frepeat]:    https://randomseed.pl/software/futils/#frepeat
[mapply]:     https://randomseed.pl/software/futils/#mapply

