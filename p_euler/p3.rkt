#lang racket

(require "number.rkt")
;;; Largest prime factor
;;;
;;; The prime factors of 13195 are 5, 7, 13 and 29.
;;; What is the largest prime factor of the number 600851475143 ?


;; calculate the largest prime factor of num
(define (largest-prime-factor num)
  (let ([prime-vec (do-sieve! (make-vector (+ 1 (exact-floor (sqrt num))) true))])
    (define (helper cur-p max-p)
      (let ([next-p (next-prime prime-vec (+ 1 cur-p))]
            [cur-max-f (if (eqv? 0 (remainder num cur-p))
                           cur-p
                           max-p)])
        (if (eqv? -1 next-p)
            cur-max-f
            (helper next-p cur-max-f))))
    (helper 2 1)))

(largest-prime-factor 600851475143)


;;;

(define (smallest-multiple n)
  (let ([prime-vec (do-sieve! (make-vector (+ 1 n) true))])
    (foldl * 1 (filter (lambda (k)
                         (vector-ref prime-vec k))
                       (stream->list (in-range 1 (+ 1 n)))))))

(define (get-prime-factors prime-vec)
  (filter (lambda (k)
            (vector-ref prime-vec k))
          (stream->list (in-range 1 (vector-length prime-vec)))))

(define prime-vec (do-sieve! (make-vector 21 true)))
(define prime-factors (get-prime-factors prime-vec))
(display prime-factors)


(define (first-of pred lst)
  (cond ((null? lst) '())
        ((pred (first lst)) (first lst))
        (else (first-of pred (rest lst)))))

(define (factorize prime-factors n)
  (define (get-one-factor v prime-fs)
    (first-of (lambda (k)
                (eqv? 0 (remainder v k)))
              prime-fs))
  (define (helper v fs)
    (if (eqv? 1 v)
        fs
        (let ([factor (get-one-factor v prime-factors)])
          (helper (/ v factor)
                  (cons factor fs)))))
  (helper n '()))

(define s (smallest-multiple 20))
(filter (lambda (k)
          (not (eqv? 0 (remainder s k))))
        (range 1 20))

(define all-factors
  (map (lambda (k)
         (factorize prime-factors k))
       (range 2 21)))
