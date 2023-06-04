# Changelog

## v2.3.2 - unreleased

### Maintenance

- Updated gradlew and development dependencies

## v2.3.1 - 2023-04-07

### Maintenance

- Tweaked docs and tests

## v2.3.0 - 2023-03-23

### Added

- `generateOrAbort()` and `generateOrAbortCore()` to `Scru128Generator`
  (formerly named as `generateNoRewind()` and `generateCoreNoRewind()`)
- `Scru128Generator#generateOrResetCore()`

### Deprecated

- `Scru128Generator#generateCore()`
- `Scru128Generator#getLastStatus()` and `Scru128Generator.Status`

## v2.2.1 - 2023-03-19

### Added

- `generateNoRewind()` and `generateCoreNoRewind(long, long)` to
  `Scru128Generator` (experimental)

### Maintenance

- Updated gradlew and compile-time/development dependencies
- Improved documentation about generator method flavors

## v2.2.0 - 2022-12-23

### Added

- Iterable and Iterator implementations to `Scru128Generator` to make it work as
  infinite iterator

### Maintenance

- Updated gradlew and compile-time/development dependencies

## v2.1.2 - 2022-06-11

### Fixed

- `generateCore()` to update `counter_hi` when `timestamp` passed < 1000

## v2.1.1 - 2022-05-23

### Fixed

- `generateCore()` to reject zero as `timestamp` value

## v2.1.0 - 2022-05-22

### Added

- `generateCore()` and `getLastStatus()` to `Scru128Generator`

## v2.0.0 - 2022-05-01

### Changed

- Textual representation: 26-digit Base32 -> 25-digit Base36
- Field structure: { `timestamp`: 44 bits, `counter`: 28 bits, `per_sec_random`:
  24 bits, `per_gen_random`: 32 bits } -> { `timestamp`: 48 bits, `counter_hi`:
  24 bits, `counter_lo`: 24 bits, `entropy`: 32 bits }
- Timestamp epoch: 2020-01-01 00:00:00.000 UTC -> 1970-01-01 00:00:00.000 UTC
- Counter overflow handling: stall generator -> increment timestamp
- `Scru128Id.serialVersionUID`: 3L -> 4L

### Removed

- `Scru128.setLogger()` as counter overflow is no longer likely to occur
- `Scru128.TIMESTAMP_BIAS`
- `Scru128Id#getCounter()`, `Scru128Id#getPerSecRandom()`, `Scru128Id#getPerGenRandom()`

### Added

- `Scru128Id#getCounterHi()`, `Scru128Id#getCounterLo()`, `Scru128Id#getEntropy()`

## v1.0.0 - 2022-01-03

- Initial stable release
