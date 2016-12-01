#import "MGLMapViewTests.h"

#if TARGET_OS_IPHONE
#import "MGLMapView+MGLCustomStyleLayerAdditions.h"
#endif

@interface MGLLayerTests : MGLMapViewTests

@end

@implementation MGLLayerTests

- (void)testAddDuplicateLayers {
    //Add a source
    MGLVectorSource *source = [[MGLVectorSource alloc] initWithIdentifier:@"my-source" URL:[NSURL URLWithString:@"mapbox://mapbox.mapbox-terrain-v2"]];
    [self.mapView.style addSource: source];
    
    //Prepare layers
    MGLFillStyleLayer *layer1 = [[MGLFillStyleLayer alloc] initWithIdentifier:@"my-layer" source:source];
    MGLFillStyleLayer *layer2 = [[MGLFillStyleLayer alloc] initWithIdentifier:@"my-layer" source:source];
    MGLFillStyleLayer *layer3 = [[MGLFillStyleLayer alloc] initWithIdentifier:@"my-layer" source:source];
    
    //Add initial layer
    [self.mapView.style addLayer:layer1];
    
    //Try to add the duplicate
    XCTAssertThrowsSpecific([self.mapView.style addLayer:layer2], NSException);
    
    //Try to insert the duplicate
    XCTAssertThrowsSpecific([self.mapView.style insertLayer:layer3 belowLayer:layer1], NSException);
    
    #if TARGET_OS_IPHONE
    //Try to insert a duplicate custom layer
    MGLCustomStyleLayerPreparationHandler preparationHandler = ^{};
    MGLCustomStyleLayerDrawingHandler drawingHandler = ^(CGSize size, CLLocationCoordinate2D centerCoordinate, double zoomLevel, CLLocationDirection direction, CGFloat pitch, CGFloat perspectiveSkew) {
    };
    MGLCustomStyleLayerCompletionHandler completionHander = ^{};

    XCTAssertThrowsSpecific([self.mapView insertCustomStyleLayerWithIdentifier:@"my-layer" preparationHandler:preparationHandler drawingHandler:drawingHandler completionHandler: completionHander belowStyleLayerWithIdentifier:nil], NSException);
    #endif
}

@end
