#import "MGLMapViewTests.h"

@interface MGLSourceTests : MGLMapViewTests

@end

@implementation MGLSourceTests

- (void)testDuplicateSources {
    MGLVectorSource *source1 = [[MGLVectorSource alloc] initWithIdentifier:@"my-source" URL:[NSURL URLWithString:@"mapbox://mapbox.mapbox-terrain-v2"]];
    MGLVectorSource *source2 = [[MGLVectorSource alloc] initWithIdentifier:@"my-source" URL:[NSURL URLWithString:@"mapbox://mapbox.mapbox-terrain-v2"]];

    //Add initial source
    [self.mapView.style addSource: source1];
    
    //Try to add the duplicate
    XCTAssertThrows([self.mapView.style addSource: source2], NSException);
}

@end
