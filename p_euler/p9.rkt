#lang racket

;;; Special Pythagorean triplet
;;;
;;; A Pythagorean triplet is a set of three natural numbers, a < b < c, for which,
;;;  a^2 + b^2 = c^2
;;; e.g. 3^2 + 4^2 = 5^2
;;; There exists exactly one Pythagorean triplet for which a + b + c = 1000.
;;; Find the product abc.

(define (is-pythagorean-triplet x y z)
  (eqv? (+ (* x x) (* y y))
        (* z z)))

(define (get-triplet n)
  (let ([result '()]
        [found false])
    (do ([x1 1 (+ 1 x1)])
      ((or (>= x1 n) found))
      (do ([y1 1 (+ 1 y1)])
        ((or (>= y1 n) found))
        (if (is-pythagorean-triplet x1 y1 (- n x1 y1))
            (begin (set! result (list x1 y1 (- n x1 y1)))
                   (set! found true))
            null)))
    (values (apply * result) result)))

(get-triplet (+ 3 4 5))
(get-triplet 1000)

        
        