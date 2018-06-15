#lang racket


;;; 10001st prime
;;;
;;; By listing the first six prime numbers: 2, 3, 5, 7, 11, and 13,
;;; we can see that the 6th prime is 13.
;;; What is the 10 001st prime number?

(require "number.rkt")
;;; https://primes.utm.edu/howmany.html
;;; (x/log x)(1 + 0.992/log x) < pi(x) <(x/log x)(1 + 1.2762/log x)
;;; where pi(x) is the number of primes less than x

(define (prime-count-lower-bound n)
  (* (/ n (log n))
     (+ 1 (/ 1.2762 (log n)))))

(define (lower-bound-to-check-for-primes nth-prime)
  (define (helper c)
    (if (> (prime-count-lower-bound c) nth-prime)
        c
        (helper (* c 2))))
  (helper 4))

(define (get-nth-prime n n-check)
  (let ([prime-vec (do-sieve! (make-vector n-check true))])
    (first
     (list-ref (filter (lambda (p) (second p))
                       (map (lambda (i) (list i (vector-ref prime-vec i)))
                            (range 0 n-check)))
               (- n 1))))) ;; it is zero based, so we need to subtract 1.

(let [(n-to-check (lower-bound-to-check-for-primes 10001))]
  (get-nth-prime 10001 n-to-check))