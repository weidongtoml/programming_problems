#lang racket

(require "number.rkt")

;;; Summation of primes
;;; The sum of the primes below 10 is 2 + 3 + 5 + 7 = 17.
;;; Find the sum of all the primes below two million.

(define (sum-of-primes-less-than n)
  (let ([prime-vec (vector-filter identity
                                  (do-sieve! (list->vector (range 0 n))))])
    (for/fold ([sum 0])
              ([p prime-vec])
      (+ sum p))))

(sum-of-primes-less-than 10)

(sum-of-primes-less-than 2000000)