## Daria

<p align="center">
  <img src="https://raw.githubusercontent.com/andiogenes/daria/media/images/logo.png" />
</p>

Toy programming language with naive pattern matching design and implementation.

### Code samples
```
; Boolean logic
and :true :true = :true
and _ _ = :false

or :false :false = :false
or _ _ = :true

> and :true :false ; => :false
> and :false :true ; => :false
> or :true :false ; => :true
> or :false :true => :true
> and (or :true :false) (or :false :true) ; => :true

; Identity function
i x = x

> i :foo ; => :foo
> i :bar ; => :bar
> i :baz ; => :baz
> i (or :false :true) ; => :true
```

### Feedback
You can suggest your ideas and comment the code via [Issues](https://github.com/andiogenes/daria/issues).

### Status
[![travis-ci](https://travis-ci.com/andiogenes/daria.svg?branch=master)](https://travis-ci.com/github/andiogenes/daria)
[![codecov](https://codecov.io/gh/andiogenes/daria/branch/master/graph/badge.svg)](https://codecov.io/gh/andiogenes/daria)
