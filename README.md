# MiniDB

A minimal database built step-by-step to understand storage engine internals.

## Stages
1. Append-only log + in-memory index
2. Thread-safe writes
3. Binary record format
4. Segment files
5. Compaction
6. Comparison with B+Tree

Each stage is preserved using Git tags.

## Stage 1 — Thread-safe writes (Serialized append)

### Problem
The initial append-only storage engine was not safe under concurrent access.
If multiple threads attempted to write simultaneously, the following issues could occur:

- Two threads could observe the same file length and write at the same offset
- Interleaved writes could corrupt on-disk data
- The in-memory index could point to incorrect offsets
- Reads could seek to wrong locations, causing silent data corruption

These race conditions are non-deterministic and extremely difficult to debug.

---

### Solution
All write operations are now **serialized** using a dedicated write lock.
Only one thread may perform the following sequence at a time:

1. Seek to end of file
2. Append record
3. Publish the byte offset in the in-memory index

Reads remain concurrent and lock-free.

---

### Design Decisions
- A dedicated `writeLock` is used instead of `synchronized(this)` to avoid
  accidental lock contention and clearly express intent.
- The in-memory index uses `ConcurrentHashMap` to safely allow concurrent reads
  while writes are in progress.
- No durability or crash-safety guarantees are introduced at this stage;
  this stage focuses strictly on **thread-level correctness**.

---

### Invariant Introduced
> **No two writes can ever overlap in file space, and every published offset
> always points to a fully written record.**

All subsequent stages (binary records, segmentation, compaction) depend on this invariant.

---

### What This Stage Does NOT Solve
- Partial writes due to crashes
- Durability guarantees (`fsync`)
- Recovery from torn records
- On-disk format stability

These are intentionally deferred to later stages.

---

### Tests
- Concurrent write tests validate that multiple writers cannot corrupt the file
  or index.
- Read-after-write correctness is preserved under concurrent access.

## Stage 2 — Binary record format (Crash-safe records)

### Problem
In Stage 1, records were written using a text-based format.  
If the process or machine crashed while writing a record, the database could end up with:

- Partially written records at the end of the file
- Ambiguous record boundaries
- Inability to distinguish valid data from corruption
- Unsafe index reconstruction on restart

Text delimiters (such as commas or newlines) are not sufficient for reliable recovery.

---

### Solution
Records are now stored in a **binary, self-describing format**:

| keyLength (4 bytes) | valueLength (4 bytes) | key bytes | value bytes |

Each record explicitly declares its size, allowing the storage engine to:
- Know exactly where a record starts and ends
- Safely skip incomplete records
- Reliably rebuild the in-memory index on startup

---

### Design Decisions
- Record lengths are written **before** the data to allow safe forward scanning.
- Offsets always point to the **start of a record header**.
- Startup recovery scans records sequentially and stops cleanly on `EOFException`,
  ignoring any partially written trailing record.
- The binary format supports arbitrary (non-text) keys and values.

---

### Invariant Introduced
> **Every record has an unambiguous boundary, and a crash can never cause a
> partially written record to be interpreted as valid data.**

This invariant enables safe recovery and is a prerequisite for segmentation
and compaction.

---

### What This Stage Does NOT Solve
- Durability guarantees (`fsync`)
- Checksums or corruption detection beyond record boundaries
- Segment rotation or compaction
- Write-ahead logging (WAL)

These are intentionally deferred to later stages.

---

### Tests
- Recovery tests verify that incomplete records at the end of the file are
  ignored after restart.
- Existing read/write correctness tests continue to pass with the binary format.