#lang racket

;;; Problem 1: Multiples of 3 and 5
;;;
;;; If we list all the natural numbers below 10 that are multiples of 3 or 5,
;;; we get 3, 5, 6 and 9. The sum of these multiples is 23.
;;; Find the sum of all the multiples of 3 or 5 below 1000.

(define (multiple? v)
  (or (eqv? 0 (remainder v 3))
      (eqv? 0 (remainder v 5))))

(define (solution-1 max)
  (foldl + 0 (filter multiple?
                     (build-list max values))))

(define (solution-2 max)
  (define (helper acc m)
    (if (eqv? 2 m)
        acc
        (helper (+ acc
                   (if (multiple? m)
                       m
                       0))
                (- m 1))))
  (helper 0 (- max 1)))

(solution-1 1000)
(solution-2 1000)
;(time (solution-2 100000000))
